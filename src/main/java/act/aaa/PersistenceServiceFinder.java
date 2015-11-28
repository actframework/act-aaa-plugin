package act.aaa;

import act.Act;
import act.ActComponent;
import act.app.App;
import act.app.AppByteCodeScanner;
import act.util.SubTypeFinder;
import org.osgl._;
import org.osgl.aaa.AAAPersistentService;
import org.osgl.aaa.AuthorizationService;
import org.osgl.exception.NotAppliedException;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

@ActComponent
public class PersistenceServiceFinder extends ServiceFinderBase<AAAPersistentService> {

    public static final String JOB_ID = "__aaa_load_persistence_service";

    public PersistenceServiceFinder() {
        super(AAAPersistentService.class);
    }

    @Override
    protected String jobId() {
        return JOB_ID;
    }

    @Override
    protected void handleFound(Class<AAAPersistentService> serviceType, App app) {
        AAAPersistentService service = app.newInstance(serviceType);
        plugin().buildService(app, service);
    }
}