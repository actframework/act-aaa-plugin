package act.aaa;

import act.app.ActionContext;
import act.app.App;
import act.app.AppServiceBase;
import act.handler.RequestHandler;
import act.handler.builtin.controller.ActionHandlerInvoker;
import act.handler.builtin.controller.Handler;
import act.handler.builtin.controller.RequestHandlerProxy;
import act.handler.builtin.controller.impl.ReflectedHandlerInvoker;
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
import java.util.Set;

import static act.aaa.AAAPlugin.AAA_USER;
import static act.aaa.AAAPlugin.CTX_KEY;

class AAAService extends AppServiceBase<AAAService> {
    private List<AAAPlugin.Listener> listeners = C.newList();
    private Set<Object> needsAuthentication = C.newSet();
    private Set<Object> noAuthentication = C.newSet();

    AuthenticationService authenticationService;
    AuthorizationService authorizationService;
    AAAPersistentService persistentService;

    AAAService(App app) {
        super(app);
    }

    @Override
    protected void releaseResources() {
        listeners.clear();
        needsAuthentication.clear();
        noAuthentication.clear();
    }

    public void sessionResolved(H.Session session, ActionContext context) {
        AAAContext aaaCtx = createAAAContext(session);
        Principal p = resolvePrincipal(aaaCtx, context);
        ensureAuthenticity(p, context);
    }

    private AAAContext createAAAContext(H.Session session) {
        AAAContext ctx = new SimpleAAAContext(authenticationService, authorizationService, persistentService);
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
        RequestHandler h = ctx.attribute(ActionContext.ATTR_HANDLER);
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
            if (null == AnnotationUtil.findAnnotation(aClass, NoAuthenticate.class) &&
                    null == AnnotationUtil.findAnnotation(method, NoAuthenticate.class)) {
                requireAuthentication = true;
                throw _.breakOut(true);
            }
            return null;
        }
    }

}
