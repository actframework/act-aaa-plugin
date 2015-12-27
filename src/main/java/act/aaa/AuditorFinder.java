package act.aaa;

import act.ActComponent;
import act.app.App;
import org.osgl.aaa.Auditor;
import org.osgl.aaa.AuthenticationService;

@ActComponent
public class AuditorFinder extends ServiceFinderBase<Auditor> {

    public static final String JOB_ID = "__aaa_load_auditor_service";

    public AuditorFinder() {
        super(Auditor.class);
    }

    @Override
    protected String jobId() {
        return JOB_ID;
    }

    @Override
    protected void handleFound(Class<Auditor> auditorClass, App app) {
        Auditor auditor = app.newInstance(auditorClass);
        plugin().buildService(app, auditor);
    }
}
