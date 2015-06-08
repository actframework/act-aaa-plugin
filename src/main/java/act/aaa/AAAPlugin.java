package act.aaa;

import act.Destroyable;
import act.app.App;
import act.app.AppContext;
import act.app.event.AppEvent;
import act.app.event.AppEventListener;
import act.util.SessionManager;
import org.osgl.aaa.AAAPersistentService;
import org.osgl.aaa.AuthenticationService;
import org.osgl.aaa.AuthorizationService;
import org.osgl.aaa.Principal;
import org.osgl.http.H;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AAAPlugin extends SessionManager.Listener implements Destroyable {

    public static final String CTX_KEY = "AAA_CTX";
    public static final String AAA_USER = "__aaa_user__";

    private ConcurrentMap<App, AAAService> services = new ConcurrentHashMap<App, AAAService>();


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
        services.clear();
    }

    public void buildService(App app, AuthenticationService service) {
        AAAService aaa = services.get(app);
        if (null == aaa) {
            aaa = initializeAAAService(app);
            aaa.authenticationService = service;
        }
    }

    public void buildService(App app, AuthorizationService service) {
        AAAService aaa = services.get(app);
        if (null == aaa) {
            aaa = initializeAAAService(app);
            aaa.authorizationService = service;
        }
    }

    public void buildService(App app, AAAPersistentService service) {
        AAAService aaa = services.get(app);
        if (null == aaa) {
            aaa = initializeAAAService(app);
            aaa.persistentService = service;
        }
    }

    private AAAService initializeAAAService(final App app) {
        AAAService svc = new AAAService(app);
        services.put(app, svc);
        app.eventManager().register(new AppEventListener() {
            @Override
            public void handleAppEvent(AppEvent event) {
                if (event == AppEvent.STOP) {
                    services.remove(app);
                }
            }
        });
        return svc;
    }

    @Override
    public void sessionResolved(H.Session session, AppContext context) {
        AAAService service = services.get(context.app());
        service.sessionResolved(session, context);
    }

    public interface Listener {
        void principalResolved(Principal p, AppContext context);
    }
}
