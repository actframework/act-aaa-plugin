package act.aaa;

import act.app.App;
import act.app.AppByteCodeScanner;
import act.util.SubTypeFinder;
import org.osgl._;
import org.osgl.aaa.AAA;
import org.osgl.exception.NotAppliedException;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

/**
 * This class scans source code and byte code to locate {@link org.osgl.aaa.DynamicPermissionCheckHelper}
 */
public class DynamicPermissionCheckHelperFinder extends SubTypeFinder {
    protected DynamicPermissionCheckHelperFinder() {
        super(DynamicPermissionCheckHelperBase.class, new _.F2<App, String, Map<Class<? extends AppByteCodeScanner>, Set<String>>>() {
            public Map<Class<? extends AppByteCodeScanner>, Set<String>> apply(final App app, final String className) throws NotAppliedException, _.Break {
                final Class<? extends DynamicPermissionCheckHelperBase> c = _.classForName(className, app.classLoader());
                if (Modifier.isAbstract(c.getModifiers())) {
                    return null;
                }
                app.jobManager().afterAppStart(new Runnable() {
                    @Override
                    public void run() {
                        DynamicPermissionCheckHelperBase helper = app.newInstance(c);
                        AAA.registerDynamicPermissionChecker(helper, helper.getTargetClass());
                    }
                });
                return null;
            }
        });
    }

    @Override
    public boolean load() {
        return true;
    }
}
