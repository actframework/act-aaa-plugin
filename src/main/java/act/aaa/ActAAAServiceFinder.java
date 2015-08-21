package act.aaa;

import act.Act;
import act.app.App;
import act.app.AppByteCodeScanner;
import act.app.event.AppCodeScanned;
import act.app.event.AppEventId;
import act.event.AppEventListenerBase;
import act.util.SubTypeFinder;
import org.osgl._;
import act.aaa.ActAAAService;
import org.osgl.aaa.AuthenticationService;
import org.osgl.exception.NotAppliedException;

import java.lang.reflect.Modifier;
import java.util.EventObject;
import java.util.Map;
import java.util.Set;

public class ActAAAServiceFinder extends ServiceFinderBase<ActAAAService> {

    public static final String JOB_ID = "__aaa_load_act_aaa_service";

    public ActAAAServiceFinder() {
        super(ActAAAService.class);
    }

    @Override
    protected String jobId() {
        return JOB_ID;
    }

    @Override
    protected void handleFound(Class<ActAAAService> serviceType, App app) {
        ActAAAService service = app.newInstance(serviceType);
        plugin().buildService(app, service);
    }
}
