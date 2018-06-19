package act.aaa.util;

/*-
 * #%L
 * ACT AAA Plugin
 * %%
 * Copyright (C) 2015 - 2018 ActFramework
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

import act.aaa.ActAAAService;
import org.osgl.$;
import org.osgl.aaa.Privilege;
import org.osgl.util.C;
import org.osgl.util.S;
import org.osgl.util.StringTokenSet;

import java.lang.reflect.Method;
import java.util.Set;

public class AAAAdaptor extends ActAAAService.Base {

    private boolean passwordTypeIsCharArray;
    private Method passwordVerifier;
    private Method roleProviderMethod;
    private Method permissionProviderMethod;
    private Method privilegeProviderMethod;

    public AAAAdaptor(Class userType, boolean passwordTypeIsCharArray, Method passwordVerifier, Method roleProviderMethod, Method permissionProviderMethod, Method privilegeProviderMethod) {
        super(userType);
        this.passwordTypeIsCharArray = passwordTypeIsCharArray;
        this.passwordVerifier = passwordVerifier;
        this.roleProviderMethod = roleProviderMethod;
        this.permissionProviderMethod = permissionProviderMethod;
        this.privilegeProviderMethod = privilegeProviderMethod;
    }

    @Override
    protected boolean verifyPassword(Object user, char[] password) {
        if (passwordTypeIsCharArray) {
            return $.invokeVirtual(user, passwordVerifier, password);
        } else {
            String passwordStr = new String(password);
            return $.invokeVirtual(user, passwordVerifier, passwordStr);
        }
    }

    @Override
    protected Set<String> rolesOf(Object user) {
        if (null == roleProviderMethod) {
            return C.Set();
        }
        String roles = $.invokeVirtual(user, roleProviderMethod);
        if (S.blank(roles)) {
            return C.Set();
        }
        return C.Set(S.fastSplit(roles, StringTokenSet.SEPARATOR));
    }

    @Override
    protected Set<String> permissionsOf(Object user) {
        if (null == permissionProviderMethod) {
            return C.Set();
        }
        String permissions = $.invokeVirtual(user, permissionProviderMethod);
        if (S.blank(permissions)) {
            return C.Set();
        }
        return C.Set(S.fastSplit(permissions, StringTokenSet.SEPARATOR));
    }

    @Override
    protected Integer privilegeOf(Object user) {
        if (null == privilegeProviderMethod) {
            return null;
        }
        Object o = $.invokeVirtual(user, privilegeProviderMethod);
        if (null == o) {
            return null;
        }
        if (o instanceof Integer) {
            return (Integer) o;
        }
        Privilege p = AAALookup.privilege((String) o);
        return null == p ? null : p.getLevel();
    }

}
