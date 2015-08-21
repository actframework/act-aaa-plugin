package act.aaa;

import act.Act;
import act.app.App;
import act.app.AppByteCodeScanner;
import act.util.SubTypeFinder;
import org.osgl._;
import org.osgl.aaa.AAA;
import org.osgl.aaa.AuthenticationService;
import org.osgl.aaa.AuthorizationService;
import org.osgl.exception.NotAppliedException;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

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
