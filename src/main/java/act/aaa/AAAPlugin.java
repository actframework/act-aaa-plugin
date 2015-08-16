package act.aaa;

import act.Destroyable;
import act.app.ActionContext;
import act.app.App;
import act.app.event.AppEventId;
import act.app.event.AppStop;
import act.di.DiBinder;
import act.event.AppEventListenerBase;
import act.util.SessionManager;
import org.osgl.aaa.AAAPersistentService;
import org.osgl.aaa.AuthenticationService;
import org.osgl.aaa.AuthorizationService;
import org.osgl.aaa.Principal;
import org.osgl.http.H;

import java.util.EventObject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AAAPlugin extends SessionManager.Listener implements Destroyable {

    public static final String CTX_KEY = "AAA_CTX";
    public static final String AAA_USER = "__aaa_user__";

    private ConcurrentMap<App, AAAService> services = new ConcurrentHashMap<App, AAAService>();

    @Override
    protected void releaseResources() {
        services.clear();
    }

    public void buildService(App app, ActAAAService service) {
        AAAService aaa = initializeAAAService(app, service);
        aaa.persistentService = new DefaultPersistenceService(service);
        aaa.authenticationService = service;
    }

    public void buildService(App app, AuthenticationService service) {
        AAAService aaa = initializeAAAService(app, null);
        aaa.authenticationService = service;
    }

    public void buildService(App app, AuthorizationService service) {
        AAAService aaa = initializeAAAService(app, null);
        aaa.authorizationService = service;
    }

    public void buildService(App app, AAAPersistentService service) {
        AAAService aaa = initializeAAAService(app, null);
        aaa.persistentService = service;
    }

    private AAAService initializeAAAService(final App app, final ActAAAService appSvc) {
        AAAService svc = services.get(app);
        if (null != svc) {
            return svc;
        }
        svc = null == appSvc ? new AAAService(app) : new AAAService(app, appSvc);
        services.put(app, svc);
        app.eventBus().bind(AppEventId.STOP, new AppEventListenerBase<AppStop>("aaa-stop") {
            @Override
            public void on(AppStop event) {
                services.remove(app);
            }
        }).bind(AppEventId.PRE_START, new AppEventListenerBase() {
            @Override
            public void on(EventObject event) throws Exception {
                app.eventBus().emit(new DiBinder<AAAPersistentService>(this, AAAPersistentService.class){
                    @Override
                    public AAAPersistentService resolve(App app) {
                        return app.service(AAAService.class).persistentService();
                    }
                });
                app.eventBus().emit(new DiBinder<AuthorizationService>(this, AuthorizationService.class){
                    @Override
                    public AuthorizationService resolve(App app) {
                        return app.service(AAAService.class).authorizationService;
                    }
                });
                app.eventBus().emit(new DiBinder<AuthenticationService>(this, AuthenticationService.class){
                    @Override
                    public AuthenticationService resolve(App app) {
                        return app.service(AAAService.class).authenticationService;
                    }
                });
            }
        });
        return svc;
    }

    @Override
    public void sessionResolved(H.Session session, ActionContext context) {
        AAAService service = services.get(context.app());
        service.sessionResolved(session, context);
    }

    public interface Listener {
        /**
         * Fired when {@link Principal} is resolved from session
         * @param p the principal. Will be {@code null} if no principal found
         * @param context the current action context
         */
        void principalResolved(Principal p, ActionContext context);
    }
}
