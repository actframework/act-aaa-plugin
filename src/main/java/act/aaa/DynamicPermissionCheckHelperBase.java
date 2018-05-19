package act.aaa;

/*-
 * #%L
 * ACT AAA Plugin
 * %%
 * Copyright (C) 2015 - 2017 ActFramework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.osgl.$;
import org.osgl.aaa.*;
import org.osgl.util.C;
import org.osgl.util.Generics;

import java.lang.reflect.Type;
import java.util.List;

public abstract class DynamicPermissionCheckHelperBase<T> implements DynamicPermissionCheckHelper<T>{

    private Class<T> targetType;

    public DynamicPermissionCheckHelperBase() {
        List<Type> typeParams = Generics.typeParamImplementations(getClass(), DynamicPermissionCheckHelperBase.class);
        targetType = (Class<T>) typeParams.get(0);
    }

    public DynamicPermissionCheckHelperBase(Class<T> targetType) {
        this.targetType = $.requireNotNull(targetType);
    }

    public Class<T> getTargetClass() {
        return targetType;
    }

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
            perms.add($.requireNotNull(p));
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
