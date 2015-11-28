package act.aaa;

import act.ActComponent;
import act.app.App;
import org.osgl.aaa.AuthenticationService;

@ActComponent
public class AuthenticationServiceFinder extends ServiceFinderBase<AuthenticationService> {

    public static final String JOB_ID = "__aaa_load_authentication_service";

    public AuthenticationServiceFinder() {
        super(AuthenticationService.class);
    }

    @Override
    protected String jobId() {
        return JOB_ID;
    }

    @Override
    protected void handleFound(Class<AuthenticationService> serviceType, App app) {
        AuthenticationService service = app.newInstance(serviceType);
        plugin().buildService(app, service);
    }
}
