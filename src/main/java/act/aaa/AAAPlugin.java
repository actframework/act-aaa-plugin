package act.aaa;

import act.Destroyable;
import act.app.AppContext;
import act.handler.RequestHandler;
import act.handler.builtin.AlwaysForbidden;
import act.handler.builtin.controller.ActionHandlerInvoker;
import act.handler.builtin.controller.Handler;
import act.handler.builtin.controller.RequestHandlerProxy;
import act.handler.builtin.controller.impl.ReflectedHandlerInvoker;
import act.util.DestroyableBase;
import act.util.SessionManager;
import org.osgl._;
import org.osgl.aaa.*;
import org.osgl.aaa.impl.SimpleAAAContext;
import org.osgl.exception.NotAppliedException;
import org.osgl.http.H;
import org.osgl.mvc.result.Forbidden;
import org.osgl.util.C;
import org.osgl.util.S;

import java.lang.reflect.Method;
import java.util.List;

public class AAAPlugin extends SessionManager.Listener implements Destroyable {

    public static final String CTX_KEY = "AAA_CTX";
    public static final String AAA_USER = "__aaa_user__";

    private List<Listener> aaaListeners = C.newList();
    private C.Set<Object> needsAuthentication = C.newSet();
    private C.Set<Object> noAuthentication = C.newSet();

    AuthenticationService authenticationService;
    AuthorizationService authorizationService;
    AAAPersistentService persistentService;

    private boolean destroyed;

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void destroy() {
        if (destroyed) {
            return;
        }
        destroyed = true;
        aaaListeners.clear();
        needsAuthentication.clear();
        noAuthentication.clear();
        authenticationService = null;
        authorizationService = null;
        persistentService = null;
    }

    @Override
    public void sessionResolved(H.Session session, AppContext context) {
        AAAContext aaaCtx = createAAAContext(session);
        Principal p = resolvePrincipal(aaaCtx, context);
        ensureAuthenticity(p, context);
    }

    @Override
    public void onSessionDissolve() {
    }

    private AAAContext createAAAContext(H.Session session) {
        AAAContext ctx = new SimpleAAAContext(authenticationService, authorizationService, persistentService);
        session.put(CTX_KEY, ctx);
        return ctx;
    }

    private Principal resolvePrincipal(AAAContext aaaCtx, AppContext appCtx) {
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

    private void firePrincipalResolved(Principal p, AppContext context) {
        for (int i = 0, j = aaaListeners.size(); i < j; ++i) {
            Listener l = aaaListeners.get(i);
            l.principalResolved(p, context);
        }
    }

    private void ensureAuthenticity(Principal p, AppContext ctx) {
        RequestHandler h = ctx.attribute(AppContext.ATTR_HANDLER);
        if (null == h || (!(h instanceof RequestHandlerProxy))) {
            return;
        }
        if (null == p) {
            if (!requireAuthenticate((RequestHandlerProxy) h)) {
                return;
            }
            throw new Forbidden();
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
        } catch (_.Break b) {
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

        @Override
        public Void apply(Class<?> aClass, Method method) throws NotAppliedException, _.Break {
            if (null == AnnotationUtil.findAnnotation(aClass, NoAuthenticate.class) ||
                    null == AnnotationUtil.findAnnotation(method, NoAuthenticate.class)) {
                requireAuthentication = true;
                throw _.breakOut(true);
            }
            return null;
        }
    }

    public interface Listener {
        void principalResolved(Principal p, AppContext context);
    }
}
