package act.aaa;

import act.ActComponent;
import act.app.App;
import org.osgl.aaa.AAAPersistentService;

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