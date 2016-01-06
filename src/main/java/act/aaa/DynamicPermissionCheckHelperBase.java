package act.aaa;

import org.osgl.$;
import org.osgl.aaa.*;
import org.osgl.util.C;

import java.util.List;

public abstract class DynamicPermissionCheckHelperBase<T> implements DynamicPermissionCheckHelper<T>{

    protected abstract Class<T> getTargetClass();

    /**
     *
     * @return
     */
    @Override
    public List<? extends Permission> permissions() {
        List<String> names = permissionNames();
        if (names.isEmpty()) {
            return C.list();
        }
        AAAContext aaa = AAA.context();
        AAAPersistentService ps = aaa.getPersistentService();
        List<Permission> perms = C.newList();
        for (String name: names) {
            Permission p = ps.findByName(name, Permission.class);
            perms.add($.notNull(p));
        }
        return perms;
    }

    /**
     * Returns a list of permission name strings. By default
     * this method will return an empty list.
     * @return permission names in a string list
     */
    protected List<String> permissionNames() {
        return C.list();
    }

}
