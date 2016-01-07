package act.aaa;

import act.ActComponent;
import act.app.ActionContext;
import act.app.App;
import act.app.AppServiceBase;
import act.app.conf.AutoConfig;
import act.conf.AppConfig;
import act.conf.ConfLoader;
import act.handler.RequestHandler;
import act.handler.builtin.controller.ActionHandlerInvoker;
import act.handler.builtin.controller.Handler;
import act.handler.builtin.controller.RequestHandlerProxy;
import act.handler.builtin.controller.impl.ReflectedHandlerInvoker;
import act.util.MissingAuthenticationHandler;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import org.osgl.$;
import org.osgl.aaa.*;
import org.osgl.aaa.impl.*;
import org.osgl.exception.NotAppliedException;
import org.osgl.http.H;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.IO;
import org.osgl.util.S;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static act.aaa.AAAConfig.ddl;
import static act.aaa.AAAConfig.loginUrl;
import static act.aaa.AAAPlugin.AAA_USER;
import static act.aaa.AAAPlugin.CTX_KEY;

@AutoConfig("aaa")
@ActComponent
public class AAAService extends AppServiceBase<AAAService> {

    public static final String CTX_AAA_CTX = "aaa_context";
    public static final boolean ALWAYS_AUTHENTICATE = true;
    public static final String ACL_FILE = "acl.yaml";

    private List<AAAPlugin.Listener> listeners = C.newList();
    private Set<Object> needsAuthentication = C.newSet();
    private Set<Object> noAuthentication = C.newSet();

    AuthenticationService authenticationService;
    AuthorizationService authorizationService;
    AAAPersistentService persistentService;
    Auditor auditor;

    AAAService(final App app) {
        super(app);
        authorizationService = new SimpleAuthorizationService();
        auditor = DumbAuditor.INSTANCE;
        postOperations(app);
    }

    AAAService(final App app, final ActAAAService appSvc) {
        super(app);
        authorizationService = new SimpleAuthorizationService();
        persistentService = new DefaultPersistenceService(appSvc);
        postOperations(app);
    }

    private void postOperations(App app) {
        app.jobManager().beforeAppStart(new Runnable() {
            @Override
            public void run() {
                loadAcl();
                registerFastJsonConfig();
            }
        });
    }

    private void loadAcl() {
        URL url = app().classLoader().getResource(ACL_FILE);
        if (null != url) {
            loadYaml(url);
        }
        String commonData = S.fmt("conf/%s/aaa_init_data.yaml", ConfLoader.common());
        url = app().classLoader().getResource(commonData);
        if (null != url) {
            loadYaml(url);
        }
        String profileData = S.fmt("conf/%s/aaa_init_data.yaml", app().profile());
        url = app().classLoader().getResource(profileData);
        if (null != url) {
            loadYaml(url);
        }
    }

    @Override
    protected void releaseResources() {
        listeners.clear();
        needsAuthentication.clear();
        noAuthentication.clear();
    }

    public AAAPersistentService persistentService() {
        return persistentService;
    }

    public void sessionResolved(H.Session session, ActionContext context) {
        AAAContext aaaCtx = createAAAContext(session);
        AAA.setContext(aaaCtx);
        Principal p = resolvePrincipal(aaaCtx, context);
        ensureAuthenticity(p, context);
    }

    public AAAContext createAAAContext() {
        return new SimpleAAAContext(authenticationService, authorizationService, persistentService, auditor);
    }

    private AAAContext createAAAContext(H.Session session) {
        AAAContext ctx = createAAAContext();
        session.put(CTX_KEY, ctx);
        return ctx;
    }

    private Principal resolvePrincipal(AAAContext aaaCtx, ActionContext appCtx) {
        String userName = appCtx.session().get(AAA_USER);
        Principal p = null;
        if (S.noBlank(userName)) {
            p = persistentService.findByName(userName, Principal.class);
            if (null == p) {
                appCtx.session().remove(AAA_USER);
            } else {
                aaaCtx.setCurrentPrincipal(p);
            }
        }
        firePrincipalResolved(p, appCtx);
        return p;
    }

    private void firePrincipalResolved(Principal p, ActionContext context) {
        for (int i = 0, j = listeners.size(); i < j; ++i) {
            AAAPlugin.Listener l = listeners.get(i);
            l.principalResolved(p, context);
        }
    }

    private void ensureAuthenticity(Principal p, ActionContext ctx) {
        if (S.eq(loginUrl, ctx.req().path())) {
            return;
        }
        RequestHandler h = ctx.attribute(ActionContext.ATTR_HANDLER);
        if (null == h || (!(h instanceof RequestHandlerProxy))) {
            return;
        }
        if (null == p) {
            if (!requireAuthenticate((RequestHandlerProxy) h)) {
                return;
            }
            AppConfig config = ctx.config();
            MissingAuthenticationHandler handler = ctx.isAjax() ? config.ajaxMissingAuthenticationHandler() : config.missingAuthenticationHandler();
            throw handler.result(ctx);
        }
    }

    private boolean requireAuthenticate(RequestHandlerProxy handler) {
        if (needsAuthentication.contains(handler)) {
            return true;
        }
        if (noAuthentication.contains(handler)) {
            return false;
        }
        AuthenticationRequirementSensor sensor = new AuthenticationRequirementSensor();
        try {
            handler.accept(sensor);
        } catch ($.Break b) {
            // ignore
        }
        boolean requireAuthentication = sensor.requireAuthentication;
        if (requireAuthentication) {
            needsAuthentication.add(handler);
        } else {
            noAuthentication.add(handler);
        }
        return requireAuthentication;
    }

    private class AuthenticationRequirementSensor implements Handler.Visitor, ReflectedHandlerInvoker.ReflectedHandlerInvokerVisitor {

        boolean requireAuthentication = false;

        @Override
        public ActionHandlerInvoker.Visitor invokerVisitor() {
            return this;
        }

        private boolean hasAnnotation(Class<? extends Annotation> a, Class<?> c, Method m) {
            return null != AnnotationUtil.findAnnotation(c, a) || null != AnnotationUtil.findAnnotation(m, a);
        }

        @Override
        public Void apply(Class<?> clazz, Method method) throws NotAppliedException, $.Break {
            if (hasAnnotation(RequireAuthentication.class, clazz, method) || hasAnnotation(RequireAuthenticate.class, clazz, method)) {
                requireAuthentication = true;
                throw $.breakOut(true);
            }
            if (ALWAYS_AUTHENTICATE) {
                if (!hasAnnotation(NoAuthentication.class, clazz, method) && !hasAnnotation(NoAuthenticate.class, clazz, method)) {
                    requireAuthentication = true;
                    throw $.breakOut(true);
                }
            }
            return null;
        }
    }

    void loadYaml(URL url) {
        try {
            String s = IO.readContentAsString(url.openStream());
            loadYamlContent(s, persistentService());
        } catch (IOException e) {
            throw E.ioException(e);
        }
    }

    void loadYaml(File file) {
        String s = IO.readContentAsString(file);
        loadYamlContent(s, persistentService());
    }

    private void registerFastJsonConfig() {
        SerializeConfig serializeConfig = SerializeConfig.getGlobalInstance();
        ParserConfig parserConfig = ParserConfig.getGlobalInstance();

        FastJsonPermissionCodec permissionCodec = new FastJsonPermissionCodec(persistentService);
        serializeConfig.put(SimplePermission.class, permissionCodec);
        parserConfig.putDeserializer(SimplePermission.class, permissionCodec);

        FastJsonPrivilegeCodec privilegeCodec = new FastJsonPrivilegeCodec(persistentService);
        serializeConfig.put(SimplePrivilege.class, privilegeCodec);
        parserConfig.putDeserializer(SimplePrivilege.class, privilegeCodec);

    }

    static void loadYamlContent(String content, AAAPersistentService store) {
        Yaml yaml = new Yaml();
        prepareStore(store);
        Object o = yaml.load(content);
        if (o instanceof Map) {
            Map<Object, Map<?, ?>> objects = $.cast(o);
            for (Object key: objects.keySet()) {
                String name = key.toString().trim();
                loadObject(name, objects, store);
            }
        }
    }

    private static final Pattern P_PRINCIPAL = Pattern.compile("(principal|prin|pn|account|acc|a)", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_ROLE = Pattern.compile("(role|ro)", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_PRIVILEGE = Pattern.compile("(privilege|priv|pi)", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_PERMISSION = Pattern.compile("(permission|perm|pe)", Pattern.CASE_INSENSITIVE);

    static void loadObject(String key,  Map<Object, Map<?, ?>> repo, AAAPersistentService store) {
        Map<?, ?> data = repo.get(key);
        String type = (String) data.get("type");
        if (null == type) type = "principal"; // default item type is principal
        if (P_PRINCIPAL.matcher(type).matches()) {
            loadPrincipal(key, data, store);
        } else if (P_ROLE.matcher(type).matches()) {
            loadRole(key, data, store);
        } else if (P_PERMISSION.matcher(type).matches()) {
            loadPermission(key, data, store);
        } else if (P_PRIVILEGE.matcher(type).matches()) {
            loadPrivilege(key, data, store);
        }
    }

    static void loadPrivilege(String name,  Map<?, ?> data, AAAPersistentService store) {
        Privilege p = store.findByName(name, Privilege.class);
        if (null != p) {
            if (!ddl.update) {
                return;
            }
        } else {
            if (!ddl.create) {
                return;
            }
        }
        int lvl = (Integer)data.get("level");
        p = new SimplePrivilege(name, lvl);
        store.save(p);
    }

    static void loadPermission(String name,  Map<?, ?> data, AAAPersistentService store) {
        Permission p = store.findByName(name, Permission.class);
        if (null != p) {
            if (!ddl.update) {
                return;
            }
        } else {
            if (!ddl.create) {
                return;
            }
        }
        boolean dyna = data.containsKey("dynamic") ? (Boolean) data.get("dynamic") : false;
        SimplePermission.Builder builder = new SimplePermission.Builder(name);
        builder.dynamic(dyna);
        List<String> sl = (List<String>) data.get("implied");
        if (null != sl) {
            for (String s0: sl) {
                Permission perm = store.findByName(s0, Permission.class);
                E.invalidConfigurationIf(null == perm, "Cannot find implied permission[%s] when loading permission[%s]", s0, name);
                builder.addImplied(perm);
            }
        }
        store.save(builder.toPermission());
    }

    static void loadRole(String name,  Map<?, ?> mm, AAAPersistentService store) {
        Role r = store.findByName(name, Role.class);
        if (null != r) {
            if (!ddl.update) {
                return;
            }
        } else {
            if (!ddl.create) {
                return;
            }
        }
        SimpleRole.Builder builder = new SimpleRole.Builder(name);
        List<String> sl = (List<String>) mm.get("permissions");
        if (null != sl) {
            for (String s0: sl) {
                Permission perm = store.findByName(s0, Permission.class);
                E.invalidConfigurationIf(null == perm, "Cannot find permission[%s] when loading principal[%s]", s0, name);
                builder.grantPermission(perm);
            }
        }
        store.save(builder.toRole());
    }

    static void loadPrincipal(String name,  Map<?, ?> mm, AAAPersistentService store) {
        Principal p = store.findByName(name, Principal.class);
        if (null != p) {
            if (!ddl.principal.update) {
                return;
            }
        } else {
            if (!ddl.principal.create) {
                return;
            }
        }
        SimplePrincipal.Builder builder = new SimplePrincipal.Builder(name);
        String s = (String) mm.get("privilege");
        if (null != s) {
            Privilege priv = store.findByName(s, Privilege.class);
            E.invalidConfigurationIf(null == priv, "Cannot find privilege[%s] when loading principal[%s]", s, name);
            builder.grantPrivilege(priv);
        }
        List<String> sl = (List<String>) mm.get("roles");
        if (null != sl) {
            for (String s0: sl) {
                Role role = store.findByName(s0, Role.class);
                E.invalidConfigurationIf(null == role, "Cannot find role[%s] when loading principal[%s]", s0, name);
                builder.grantRole(role);
            }
        }
        sl = (List<String>) mm.get("permissions");
        if (null != sl) {
            for (String s0: sl) {
                Permission perm = store.findByName(s0, Permission.class);
                E.invalidConfigurationIf(null == perm, "Cannot find permission[%s] when loading principal[%s]", s0, name);
                builder.grantPermission(perm);
            }
        }
        store.save(builder.toPrincipal());
    }

    static void prepareStore(AAAPersistentService store) {
        if (ddl.delete) {
            store.removeAll(Privilege.class);
            store.removeAll(Permission.class);
            store.removeAll(Role.class);
        }
        if (ddl.principal.delete) {
            store.removeAll(Principal.class);
        }
    }

}
