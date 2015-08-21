package act.aaa;

import act.Act;
import act.app.App;
import act.app.event.AppEventId;
import act.util.SubTypeFinder2;

public abstract class ServiceFinderBase<T> extends SubTypeFinder2<T> {

    protected ServiceFinderBase(Class<T> target) {
        super(target);
    }

    protected abstract String jobId();

    protected abstract void handleFound(Class<T> serviceType, App app);

    protected AAAPlugin plugin() {
        return Act.sessionManager().findListener(AAAPlugin.class);
    }

    @Override
    protected void found(final Class<T> target, final App app) {
        app.jobManager().on(AppEventId.DEPENDENCY_INJECTOR_LOADED, jobId(), new Runnable() {
            @Override
            public void run() {
                handleFound(target, app);
            }
        });
    }
}
