package act.aaa;

import act.Act;
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

public class PersistenceServiceFinder extends SubTypeFinder {
    public PersistenceServiceFinder() {
        super(true, true, AAAPersistentService.class, new _.F2<App, String, Map<Class<? extends AppByteCodeScanner>, Set<String>>>() {
            public Map<Class<? extends AppByteCodeScanner>, Set<String>> apply(final App app, final String className) throws NotAppliedException, _.Break {
                final Class<? extends AAAPersistentService> c = _.classForName(className, app.classLoader());
                if (Modifier.isAbstract(c.getModifiers())) {
                    return null;
                }
                app.jobManager().afterAppStart(new Runnable() {
                    @Override
                    public void run() {
                        AAAPersistentService service = app.newInstance(c);
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
