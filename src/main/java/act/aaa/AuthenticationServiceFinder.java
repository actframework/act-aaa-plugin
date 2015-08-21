package act.aaa;

import act.Act;
import act.app.App;
import act.app.AppByteCodeScanner;
import act.app.event.AppCodeScanned;
import act.app.event.AppEventId;
import act.event.AppEventListenerBase;
import act.util.SubTypeFinder;
import org.osgl._;
import org.osgl.aaa.AuthenticationService;
import org.osgl.aaa.AuthorizationService;
import org.osgl.exception.NotAppliedException;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

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
