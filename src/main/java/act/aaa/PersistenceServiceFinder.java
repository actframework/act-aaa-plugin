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
        if (DefaultPersistenceService.class.equals(serviceType)) {
            // DefaultPersistentService is not aimed to be used for dependency injection
            // however subclass of it might be implemented by app developer
            return;
        }
        AAAPersistentService service = app.newInstance(serviceType);
        plugin().buildService(app, service);
    }
}