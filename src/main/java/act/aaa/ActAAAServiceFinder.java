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
import org.osgl.exception.NotAppliedException;

import java.lang.reflect.Modifier;
import java.util.EventObject;
import java.util.Map;
import java.util.Set;

public class ActAAAServiceFinder extends SubTypeFinder {
    public ActAAAServiceFinder() {
        super(true, true, ActAAAService.class, new _.F2<App, String, Map<Class<? extends AppByteCodeScanner>, Set<String>>>() {
            public Map<Class<? extends AppByteCodeScanner>, Set<String>> apply(final App app, final String className) throws NotAppliedException, _.Break {
                final Class<? extends ActAAAService> c = _.classForName(className, app.classLoader());
                if (Modifier.isAbstract(c.getModifiers())) {
                    return null;
                }
                app.eventBus().bind(AppEventId.APP_CODE_SCANNED, new AppEventListenerBase<AppCodeScanned>() {
                    @Override
                    public void on(AppCodeScanned event) throws Exception {
                        ActAAAService service = app.newInstance(c);
                        AAAPlugin plugin = Act.sessionManager().findListener(AAAPlugin.class);
                        if (null == plugin) {
                            logger.error("AAAPlugin not found");
                        } else {
                            plugin.buildService(app, service);
                        }
                    }
                });
                return null;
            }
        });
    }
}
