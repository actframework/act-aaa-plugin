package act.aaa;

import act.Act;
import act.app.App;
import act.app.AppByteCodeScanner;
import act.util.SubTypeFinder;
import org.osgl._;
import org.osgl.aaa.AAA;
import org.osgl.aaa.AuthorizationService;
import org.osgl.exception.NotAppliedException;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

public class AuthorizationServiceFinder extends SubTypeFinder {
    public AuthorizationServiceFinder() {
        super(AuthorizationService.class, new _.F2<App, String, Map<Class<? extends AppByteCodeScanner>, Set<String>>>() {
            public Map<Class<? extends AppByteCodeScanner>, Set<String>> apply(final App app, final String className) throws NotAppliedException, _.Break {
                final Class<? extends AuthorizationService> c = _.classForName(className, app.classLoader());
                if (Modifier.isAbstract(c.getModifiers())) {
                    return null;
                }
                app.jobManager().afterAppStart(new Runnable() {
                    @Override
                    public void run() {
                        AuthorizationService service = app.newInstance(c);
                        AAAPlugin plugin = Act.sessionManager().findListener(AAAPlugin.class);
                        if (null == plugin) {
                            logger.error("AAAPlugin not found");
                        } else {
                            plugin.authorizationService = service;
                        }
                    }
                });
                return null;
            }
        });
    }
}
