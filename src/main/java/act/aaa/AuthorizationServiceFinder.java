package act.aaa;

import act.ActComponent;
import act.app.App;
import org.osgl.aaa.AuthorizationService;

@ActComponent
public class AuthorizationServiceFinder extends ServiceFinderBase<AuthorizationService> {

    public static final String JOB_ID = "__aaa_load_authorization_service";

    public AuthorizationServiceFinder() {
        super(AuthorizationService.class);
    }

    @Override
    protected String jobId() {
        return JOB_ID;
    }

    @Override
    protected void handleFound(Class<AuthorizationService> serviceType, App app) {
        AuthorizationService service = app.newInstance(serviceType);
        plugin().buildService(app, service);
    }
}
