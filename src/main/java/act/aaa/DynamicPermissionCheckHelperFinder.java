package act.aaa;

import act.ActComponent;
import act.app.App;
import act.util.SubTypeFinder2;
import org.osgl.aaa.AAA;

/**
 * This class scans source code and byte code to locate {@link org.osgl.aaa.DynamicPermissionCheckHelper}
 */
@ActComponent
public class DynamicPermissionCheckHelperFinder extends SubTypeFinder2<DynamicPermissionCheckHelperBase> {

    public DynamicPermissionCheckHelperFinder() {
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
