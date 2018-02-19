package act.aaa;

/*-
 * #%L
 * ACT AAA Plugin
 * %%
 * Copyright (C) 2015 - 2017 ActFramework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static act.aaa.AAAConfig.ddl;
import static act.aaa.AAAConfig.loginUrl;
import static act.app.ProjectLayout.Utils.file;

import act.Act;
import act.app.ActionContext;
import act.app.App;
import act.app.AppServiceBase;
import act.app.conf.AutoConfig;
import act.conf.ConfLoader;
import act.event.OnceEventListenerBase;
import act.handler.RequestHandler;
import act.handler.builtin.controller.ActionHandlerInvoker;
import act.handler.builtin.controller.Handler;
import act.handler.builtin.controller.RequestHandlerProxy;
import act.handler.builtin.controller.impl.ReflectedHandlerInvoker;
import act.util.MissingAuthenticationHandler;
import act.util.SubClassFinder;
import act.view.ActForbidden;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializeConfig;
import org.osgl.$;
import org.osgl.aaa.*;
import org.osgl.aaa.impl.*;
import org.osgl.exception.NotAppliedException;
import org.osgl.http.H;
import org.osgl.mvc.annotation.Catch;
import org.osgl.util.*;
import org.yaml.snakeyaml.Yaml;
import osgl.version.Version;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

@AutoConfig("aaa")
public class AAAService extends AppServiceBase<AAAService> {

    /**
     * Defines the version of AAA Plugin
     *
     * @see AAAPlugin#VERSION
     */
    public static final Version VERSION = AAAPlugin.VERSION;

    private static final Const<Boolean> ALWAYS_AUTHENTICATE = $.constant(true);
    private static final Const<String> ACL_FILE = $.constant("acl.yaml");
    private static final String AAA_AUTH_LIST = "aaa.authenticate.list";
    private static final Const<Boolean> ALLOW_SYS_SERVICE_ON_DEV_MODE = $.constant(false);

    private List<AAAPlugin.Listener> listeners = C.newList();
    private Set<Object> needsAuthentication = C.newSet();
    private Set<Object> noAuthentication = C.newSet();
    private Set<String> waiveAuthenticateList = C.newSet();
    private Set<String> forceAuthenticateList = C.newSet();
    private boolean allowBasicAuthentication = false;
    private boolean disabled;
    private final String sessionKeyUsername;

    private AuthenticationService authenticationService;
    private AuthorizationService authorizationService;
    private AAAPersistentService persistentService;
    private Auditor auditor;

    private OnceEventListenerBase onServiceInitialized = new OnceEventListenerBase() {
        @Override
        public boolean tryHandle(EventObject event) throws Exception {
            if (serviceInitialized()) {
                loadAcl();
                registerFastJsonConfig();
                registerDefaultContext();
            }
            return true;
        }
    };

    AAAService(final App app) {
        super(app);
        loadAuthenticateList();
        sessionKeyUsername = app.config().sessionKeyUsername();
        authorizationService = new SimpleAuthorizationService();
        auditor = DumbAuditor.INSTANCE;
        allowBasicAuthentication = app.config().basicAuthenticationEnabled();
        postOperations(app);
    }

    AAAService(final App app, final ActAAAService appSvc) {
        this(app);
        this.persistentService(new DefaultPersistentService(appSvc));
    }

    public boolean serviceInitialized() {
        return null != authenticationService && null != authorizationService && null != persistentService;
    }

    private void postOperations(App app) {
        app.eventBus().once(AAAPersistenceServiceInitialized.class, onServiceInitialized);
        app.eventBus().once(AuthenticationServiceInitialized.class, onServiceInitialized);
        app.eventBus().once(AuthorizationServiceInitialized.class, onServiceInitialized);
    }

    private void loadAuthenticateList() {
        List<String> lines = new ArrayList<String>();
        try {
            final Enumeration<URL> systemResources = Act.class.getClassLoader().getResources(AAA_AUTH_LIST);
            while (systemResources.hasMoreElements()) {
                InputStream is = systemResources.nextElement().openStream();
                String s = IO.readContentAsString(is);
                lines.addAll(
                        C.listOf(s.split("[\r\n]+"))
                                .filter(S.F.startsWith("#").negate())
                                .filter(S.F.IS_BLANK.negate()));
            }
        } catch (IOException e) {
            throw E.ioException(e);
        }
        for (String s : lines) {
            if (s.startsWith("-")) {
                s = s.substring(1);
                waiveAuthenticateList.add(s);
            }
        }
        for (String s : lines) {
            if (s.startsWith("-")) {
                continue;
            } else if (s.startsWith("+")) {
                forceAuthenticateList.add(s.substring(1));
                waiveAuthenticateList.remove(s.substring(1));
            } else {
                forceAuthenticateList.add(s);
                waiveAuthenticateList.remove(s);
            }
        }
    }

    private void loadAcl() {
        if (Act.isDev()) {
            devLoadAcl();
        } else {
            prodLoadAcl();
        }
    }

    private void devLoadAcl() {
        final String aclFile = AAAService.ACL_FILE.get();
        File resources = app().layout().resource(app().base());
        File acl = file(resources, aclFile);
        loadYaml(acl);

        File confRoot = file(resources, "/conf");

        acl = file(confRoot, aclFile);
        loadYaml(acl);

        File common = file(confRoot, ConfLoader.common());
        acl = file(common, aclFile);
        loadYaml(acl);

        File profile = file(confRoot, Act.profile());
        acl = file(profile, aclFile);
        loadYaml(acl);
    }

    private void prodLoadAcl() {
        URL url = app().classLoader().getResource(ACL_FILE.get());
        if (null != url) {
            loadYaml(url);
        }
        ClassLoader classLoader = app().classLoader();
        String confData = S.fmt("conf/%s", ACL_FILE);
        url = classLoader.getResource(confData);
        if (null != url) {
            loadYaml(url);
        }
        String commonData = S.fmt("conf/%s/%s", ConfLoader.common(), ACL_FILE);
        url = classLoader.getResource(commonData);
        if (null != url) {
            loadYaml(url);
        }
        String profileData = S.fmt("conf/%s/%s", app().profile(), ACL_FILE);
        url = classLoader.getResource(profileData);
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

    public AuthenticationService authenticationService() {
        return authenticationService;
    }

    public AuthorizationService authorizationService() {
        return authorizationService;
    }

    public Auditor auditor() {
        return auditor;
    }

    AAAService persistentService(AAAPersistentService service) {
        boolean firstLoadPersistenceService = null == this.persistentService;
        if (null != this.persistentService && service instanceof DefaultPersistentService) {
            // app's implementation should be the winner
            return this;
        }
        this.persistentService = $.notNull(service);
        if (firstLoadPersistenceService) {
            app().eventBus().trigger(new AAAPersistenceServiceInitialized(this));
        }
        return this;
    }

    AAAService authenticationService(AuthenticationService service) {
        boolean firstLoad = null == this.authenticationService;
        this.authenticationService = $.notNull(service);
        if (firstLoad) {
            app().eventBus().trigger(new AuthenticationServiceInitialized(this));
        }
        return this;
    }

    AAAService authorizationService(AuthorizationService service) {
        boolean firstLoad = null == this.authorizationService;
        this.authorizationService = $.notNull(service);
        if (firstLoad) {
            app().eventBus().trigger(new AuthorizationServiceInitialized(this));
        }
        return this;
    }

    AAAService auditor(Auditor auditor) {
        boolean firstLoad = null == this.auditor;
        this.auditor = $.notNull(auditor);
        if (firstLoad) {
            app().eventBus().trigger(new AuditorInitialized(this));
        }
        return this;
    }

    public void sessionResolved(H.Session session, ActionContext context) {
        if (disabled) {
            return;
        }
        AAAContext aaaCtx = createAAAContext();
        AAA.setContext(aaaCtx);
        try {
            Principal p = resolvePrincipal(aaaCtx, context);
            ensureAuthenticity(p, context);
        } catch (NoAccessException e) {
            throw ActForbidden.create(e);
        }
    }

    public AAAContext createAAAContext() {
        return new SimpleAAAContext(authenticationService, authorizationService, persistentService, auditor);
    }

    private Principal resolvePrincipal(AAAContext aaaCtx, ActionContext appCtx) {
        Principal p = null;

        String userName = appCtx.session().get(sessionKeyUsername);
        if (S.blank(userName)) {
            if (allowBasicAuthentication) {
                String user = appCtx.req().user();
                if (S.notBlank(user)) {
                    String password = appCtx.req().password();
                    p = authenticationService.authenticate(user, password);
                }
            }
        } else {
            p = persistentService.findByName(userName, Principal.class);
        }
        if (null == p) {
            appCtx.session().remove(sessionKeyUsername);
        } else {
            aaaCtx.setCurrentPrincipal(p);
        }
        firePrincipalResolved(p, appCtx);
        return p;
    }

    private void firePrincipalResolved(Principal p, ActionContext context) {
        for (int i = 0, j = listeners.size(); i < j; ++i) {
            AAAPlugin.Listener l = listeners.get(i);
            l.principalResolved(p, context);
        }
        if (null != p) {
            context.app().eventBus().trigger(new PrincipalResolved(p));
        }
    }

    private void ensureAuthenticity(Principal p, ActionContext ctx) {
        if (S.eq(loginUrl, ctx.req().path())) {
            return;
        }
        RequestHandler h = ctx.handler();
        if (null == h || h.sessionFree()) {
            return;
        }
        if (null == p) {
            if (!requireAuthenticate(h)) {
                return;
            }
            MissingAuthenticationHandler handler = ctx.missingAuthenticationHandler();
            handler.handle(ctx);
        }
    }

    /**
     * Rules to identify if a handler needs authentication or not:
     *
     * 1. If handler or interceptor method has `RequireAuthentication` annotation then it means it must be authenticated
     * 2. If handler or interceptor method has `NoAuthentication` annotation then it means it can waive authentication
     * 3. If handler or interceptor method doesn't have annotation then it needs to check {@link #ALWAYS_AUTHENTICATE}
     * 4. If any method is required to be authenticated then the whole logic needs to be authenticated
     *
     * @param handler the handler
     * @return `true` if it require authentication on this handler or `false` otherwise
     */
    private boolean requireAuthenticate(RequestHandler handler) {
        if (needsAuthentication.contains(handler)) {
            return true;
        }
        if (noAuthentication.contains(handler)) {
            return false;
        }
        if (!(handler instanceof RequestHandlerProxy)) {
            String actionName = handler.getClass().getName();
            boolean needAuthentication = requireAuthentication(actionName);
            if (needAuthentication) {
                needsAuthentication.add(handler);
            } else {
                noAuthentication.add(handler);
            }
            return needAuthentication;
        }
        AuthenticationRequirementSensor sensor = new AuthenticationRequirementSensor();
        try {
            ((RequestHandlerProxy)handler).accept(sensor);
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

    private boolean requireAuthentication(String actionName) {
        if (forceAuthenticateList.contains(actionName)) {
            return true;
        }
        if (waiveAuthenticateList.contains(actionName)) {
            return false;
        }
        for (String s: forceAuthenticateList) {
            if (actionName.startsWith(s) || actionName.matches(s)) {
                return true;
            }
        }
        for (String s: waiveAuthenticateList) {
            if (actionName.startsWith(s) || actionName.matches(s)) {
                return false;
            }
        }
        return (Act.isProd() || !ALLOW_SYS_SERVICE_ON_DEV_MODE.get()) && ALWAYS_AUTHENTICATE.get();
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
            if (null != method.getAnnotation(Catch.class)) {
                // skip exception catch method
                return null;
            }
            if (hasAnnotation(RequireAuthentication.class, clazz, method) || hasAnnotation(RequireAuthenticate.class, clazz, method)) {
                requireAuthentication = true;
                throw $.breakOut(true);
            }
            if (hasAnnotation(NoAuthentication.class, clazz, method) || hasAnnotation(NoAuthenticate.class, clazz, method)) {
                return null;
            }
            String actionName = S.builder(clazz.getName()).append(".").append(method.getName()).toString();
            requireAuthentication = requireAuthentication(actionName);
            if (requireAuthentication) {
                throw $.breakOut(true);
            }
            return null;
        }
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

    private void registerDefaultContext() {
        try {
            AAA.setDefaultContext(createAAAContext());
        } catch (NullPointerException e) {
            warn("Cannot create AAA context. AAA plugin disabled");
            disabled = true;
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
        if (file.exists() && file.canRead()) {
            loadYamlContent(IO.readContentAsString(file), persistentService());
        }
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

    @SuppressWarnings("unused")
    @SubClassFinder
    void loadListener(AAAPlugin.Listener listener) {
        listeners.add(listener);
    }

}
