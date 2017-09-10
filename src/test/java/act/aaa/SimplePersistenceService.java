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

import java.util.Map;

public class SimplePersistenceService implements AAAPersistentService {

    Map<Class, Map<String, AAAObject>> repo = C.newMap();

    @Override
    public void save(AAAObject aaaObject) {
        Map<String, AAAObject> db = db(aaaObject);
        db.put(aaaObject.getName(), aaaObject);
    }

    @Override
    public void remove(AAAObject aaaObject) {
        Map<String, AAAObject> db = db(aaaObject);
        db.remove(aaaObject.getName());
    }

    @Override
    public <T extends AAAObject> void removeAll(Class<T> aClass) {
        Map<String, AAAObject> db = db(aClass);
        db.clear();
    }

    @Override
    public <T extends AAAObject> T findByName(String s, Class<T> aClass) {
        Map<String, AAAObject> db = db(aClass);
        return (T) db.get(s);
    }

    @Override
    public Privilege findPrivilege(int level) {
        Map<String, AAAObject> privileges = db(Privilege.class);
        for (AAAObject obj : privileges.values()) {
            Privilege p = (Privilege) obj;
            if (p.getLevel() == level) {
                return p;
            }
        }
        return null;
    }

    @Override
    public Iterable<Privilege> allPrivileges() {
        return $.cast(db(Privilege.class).values());
    }

    @Override
    public Iterable<Permission> allPermissions() {
        return $.cast(db(Permission.class).values());
    }

    @Override
    public Iterable<Role> allRoles() {
        return $.cast(db(Role.class).values());
    }

    @Override
    public Iterable<String> allPrivilegeNames() {
        return db(Privilege.class).keySet();
    }

    @Override
    public Iterable<String> allPermissionNames() {
        return db(Permission.class).keySet();
    }

    @Override
    public Iterable<String> allRoleNames() {
        return db(Role.class).keySet();
    }

    private Map<String, AAAObject> db(Class c) {
        Class c0;
        if (Principal.class.isAssignableFrom(c)) {
            c0 = Principal.class;
        } else if (Role.class.isAssignableFrom(c)) {
            c0 = Role.class;
        } else if (Permission.class.isAssignableFrom(c)) {
            c0 = Permission.class;
        } else if (Privilege.class.isAssignableFrom(c)) {
            c0 = Privilege.class;
        } else {
            throw new IllegalArgumentException(c.getName());
        }
        Map<String, AAAObject> db = repo.get(c0);
        if (null == db) {
            db = C.newMap();
            repo.put(c0, db);
        }
        return db;
    }

    private Map<String, AAAObject> db(AAAObject o) {
        return db(o.getClass());
    }
}
