package act.aaa;

import org.osgl.aaa.DynamicPermissionCheckHelper;
import org.osgl.aaa.Permission;
import org.osgl.util.C;

import java.util.List;

public abstract class DynamicPermissionCheckHelperBase<T> implements DynamicPermissionCheckHelper<T>{

    protected abstract Class<T> getTargetClass();

    @Override
    public List<? extends Permission> permissions() {
        return C.list();
    }
}
