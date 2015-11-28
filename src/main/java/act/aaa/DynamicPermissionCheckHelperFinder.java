package act.aaa;

import act.ActComponent;
import act.app.App;
import act.app.AppByteCodeScanner;
import act.util.ClassNode;
import act.util.SubTypeFinder;
import act.util.SubTypeFinder2;
import org.osgl._;
import org.osgl.aaa.AAA;
import org.osgl.aaa.DynamicPermissionCheckHelper;
import org.osgl.exception.NotAppliedException;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

/**
 * This class scans source code and byte code to locate {@link org.osgl.aaa.DynamicPermissionCheckHelper}
 */
@ActComponent
public class DynamicPermissionCheckHelperFinder extends SubTypeFinder2<DynamicPermissionCheckHelperBase> {

    protected DynamicPermissionCheckHelperFinder() {
        super(DynamicPermissionCheckHelperBase.class);
    }

    @Override
    protected void found(final Class<DynamicPermissionCheckHelperBase> target, final App app) {
        app.jobManager().beforeAppStart(new Runnable() {
            @Override
            public void run() {
                DynamicPermissionCheckHelperBase helper = app.newInstance(target);
                AAA.registerDynamicPermissionChecker(helper, helper.getTargetClass());
            }
        });
    }

}
