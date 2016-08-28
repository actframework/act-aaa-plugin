package act.aaa;

import act.Act;
import act.app.App;
import act.app.event.AppEventId;
import act.util.SubClassFinder;
import org.osgl.aaa.*;

import javax.inject.Inject;

@SuppressWarnings("unused")
public class AAAServiceFinder<T> {

    private App app;

    @Inject
    public AAAServiceFinder(App app) {
        this.app = app;
    }

    @SubClassFinder
    public void foundActAAAService(Class<ActAAAService> serviceType) {
        ActAAAService service = app.getInstance(serviceType);
        plugin().buildService(app, service);
    }

    @SubClassFinder
    public void foundAuditorService(Class<Auditor> auditorClass) {
        Auditor auditor = app.getInstance(auditorClass);
        plugin().buildService(app, auditor);
    }

    @SubClassFinder
    public void foundAuthenticationService(Class<AuthenticationService> serviceType) {
        if (ActAAAService.class.isAssignableFrom(serviceType)) {
            return;
        }
        AuthenticationService service = app.getInstance(serviceType);
        plugin().buildService(app, service);
    }

    @SubClassFinder
    public void foundAuthorizationService(Class<AuthorizationService> serviceType) {
        AuthorizationService service = app.getInstance(serviceType);
        plugin().buildService(app, service);
    }

    @SubClassFinder(callOn = AppEventId.PRE_START)
    public void foundDynamicPermissionCheckHelper(final Class<DynamicPermissionCheckHelperBase> target) {
        DynamicPermissionCheckHelperBase helper = app.getInstance(target);
        AAA.registerDynamicPermissionChecker(helper, helper.getTargetClass());
    }

    @SubClassFinder
    public void handleFound(Class<AAAPersistentService> serviceType) {
        if (DefaultPersistenceService.class.equals(serviceType)) {
            // DefaultPersistentService is not aimed to be used for dependency injection
            // however subclass of it might be implemented by app developer
            return;
        }
        AAAPersistentService service = app.getInstance(serviceType);
        plugin().buildService(app, service);
    }

    private AAAPlugin plugin() {
        return Act.sessionManager().findListener(AAAPlugin.class);
    }

}
