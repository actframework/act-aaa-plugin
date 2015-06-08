package act.aaa;

import org.osgl.aaa.DynamicPermissionCheckHelper;

public abstract class DynamicPermissionCheckHelperBase<T> implements DynamicPermissionCheckHelper<T>{

    protected abstract Class<T> getTargetClass();

}
