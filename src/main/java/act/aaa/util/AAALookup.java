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

import act.aaa.AAAService;
import act.app.App;
import act.app.event.SysEventId;
import act.plugin.AppServicePlugin;
import org.osgl.aaa.Permission;
import org.osgl.aaa.Privilege;
import org.osgl.aaa.Role;
import org.osgl.util.C;
import org.osgl.util.S;
import org.osgl.util.StringTokenSet;

import java.util.ArrayList;
import java.util.List;

public final class AAALookup extends AppServicePlugin {

    private static AAAService aaa;

    @Override
    protected void applyTo(final App app) {
        app.jobManager().on(SysEventId.START, new Runnable() {
            @Override
            public void run() {
                AAALookup.aaa = app.getInstance(AAAService.class);
            }
        });
    }

    /**
     * Returns a list of {@link Role} from a string list of role names. The
     * `roleNames` shall be a String of names separated by {@link org.osgl.util.StringTokenSet#SEPARATOR}.
     *
     * @param roleNames the role names
     * @return a list of roles
     */
    public static List<Role> roles(String roleNames) {
        if (S.blank(roleNames)) {
            return C.list();
        }
        List<String> list = S.fastSplit(roleNames, StringTokenSet.SEPARATOR);
        List<Role> roles = new ArrayList<>();
        for (String name : list) {
            name = name.trim();
            Role role = aaa.getRole(name);
            roles.add(role);
        }
        return roles;
    }

    /**
     * Returns a list of {@link Permission} from a string list of permission names. The
     * `permissionNames` shall be a String of names separated by {@link org.osgl.util.StringTokenSet#SEPARATOR}.
     *
     * @param permissionNames the permission names
     * @return a list of permissions
     */
    public static List<Permission> permissions(String permissionNames) {
        if (S.blank(permissionNames)) {
            return C.list();
        }
        List<String> list = S.fastSplit(permissionNames, StringTokenSet.SEPARATOR);
        List<Permission> permissions = new ArrayList<>();
        for (String name : list) {
            name = name.trim();
            Permission permission = aaa.getPermission(name);
            permissions.add(permission);
        }
        return permissions;
    }

    public static Privilege privilege(String privilegeName) {
        if (S.blank(privilegeName)) {
            return null;
        }
        return aaa.getPrivilege(privilegeName);
    }
}
