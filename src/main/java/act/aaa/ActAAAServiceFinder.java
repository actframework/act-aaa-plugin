package act.aaa;

import act.ActComponent;
import act.app.App;

@ActComponent
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
