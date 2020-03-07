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

import act.util.DestroyableBase;
import org.osgl.aaa.*;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.Keyword;

import java.util.Map;
import java.util.Set;

public class DefaultPersistentService extends DestroyableBase implements AAAPersistentService {

    private ActAAAService actAAAService;

    private Map<String, Role> roles = C.newMap();
    private Map<String, Permission> permissions = C.newMap();
    private Map<String, Privilege> privileges = C.newMap();

    public DefaultPersistentService(ActAAAService ActAAAService) {
        E.NPE(ActAAAService);
        this.actAAAService = ActAAAService;
    }

    @Override
    protected void releaseResources() {
        roles.clear();
        permissions.clear();
        privileges.clear();
        actAAAService = null;
    }

    @Override
    public <T extends AAAObject> void removeAll(Class<T> aClass) {
        if (Principal.class.isAssignableFrom(aClass)) {
            actAAAService.removeAllPrincipals();
        } else if (Role.class.isAssignableFrom(aClass)) {
            roles.clear();
        } else if (Permission.class.isAssignableFrom(aClass)) {
            permissions.clear();
        } else if (Privilege.class.isAssignableFrom(aClass)) {
            privileges.clear();
        } else {
            throw E.unsupport("Unknown aaa object type: %s", aClass);
        }
    }

    @Override
    public void save(AAAObject aaaObject) {
        if (aaaObject instanceof Principal) {
            actAAAService.save((Principal) aaaObject);
        } else if (aaaObject instanceof Role) {
            roles.put(normalizeAAAObjectName(aaaObject), (Role) aaaObject);
        } else if (aaaObject instanceof Permission) {
            permissions.put(normalizeAAAObjectName(aaaObject), (Permission) aaaObject);
        } else if (aaaObject instanceof Privilege) {
            privileges.put(normalizeAAAObjectName(aaaObject), (Privilege) aaaObject);
        } else {
            throw E.unsupport("Unknown aaa object type: %s", aaaObject.getClass());
        }
    }

    public static String normalizeAAAObjectName(AAAObject aaaObject) {
        return normalizeAAAObjectName(aaaObject.getName());
    }

    public static String normalizeAAAObjectName(String name) {
        return Keyword.of(name).constantName();
    }

    @Override
    public void remove(AAAObject aaaObject) {
        throw E.unsupport();
    }

    @Override
    public <T extends AAAObject> T findByName(String name, Class<T> aClass) {
        if (Principal.class.isAssignableFrom(aClass)) {
            return (T) actAAAService.findByName(name);
        } else if (Role.class.isAssignableFrom(aClass)) {
            return (T) roles.get(normalizeAAAObjectName(name));
        } else if (Permission.class.isAssignableFrom(aClass)) {
            return (T) permissions.get(normalizeAAAObjectName(name));
        } else if (Privilege.class.isAssignableFrom(aClass)) {
            return (T) privileges.get(normalizeAAAObjectName(name));
        } else {
            throw E.unsupport("Unknown aaa object type: %s", aClass);
        }
    }

    @Override
    public Privilege findPrivilege(int level) {
        for (Privilege p : privileges.values()) {
            if (p.getLevel() == level) {
                return p;
            }
        }
        return null;
    }

    /**
     * Deprecated. Use {@link #allRoleNames()} instead
     * @return role names
     */
    @Deprecated
    public Set<String> roleNames() {
        return roles.keySet();
    }

    /**
     * Deprecated. Use {@link #allPrivilegeNames()} ()} instead
     * @return privilege names
     */
    @Deprecated
    public Set<String> privilegeNames() {
        return privileges.keySet();
    }

    /**
     * Deprecated. Use {@link #allPermissionNames()} ()} instead
     * @return permission names
     */
    @Deprecated
    public Set<String> permissionNames() {
        return permissions.keySet();
    }

    @Override
    public Iterable<Privilege> allPrivileges() {
        return privileges.values();
    }

    @Override
    public Iterable<Permission> allPermissions() {
        return permissions.values();
    }

    @Override
    public Iterable<Role> allRoles() {
        return roles.values();
    }

    @Override
    public Iterable<String> allPrivilegeNames() {
        return privileges.keySet();
    }

    @Override
    public Iterable<String> allPermissionNames() {
        return permissions.keySet();
    }

    @Override
    public Iterable<String> allRoleNames() {
        return roles.keySet();
    }
}
