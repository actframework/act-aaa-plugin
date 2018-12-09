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

import act.Act;
import act.aaa.AAAPlugin;
import act.util.NoAutoRegister;
import org.osgl.$;
import org.osgl.aaa.*;

@NoAutoRegister
public class PersistenceServiceCache implements AAAPersistentService {

    private AAAPersistentService backService;
    private AAAPlugin plugin;

    private PersistenceServiceCache(AAAPersistentService service) {
        this.backService = $.requireNotNull(service);
        this.plugin = Act.getInstance(AAAPlugin.class);
    }

    @Override
    public void save(AAAObject aaaObject) {
        backService.save(aaaObject);
        plugin.cache(aaaObject);
    }

    @Override
    public void remove(AAAObject aaaObject) {
        plugin.evictAAAObject(aaaObject);
        backService.remove(aaaObject);
    }

    @Override
    public <T extends AAAObject> void removeAll(Class<T> clz) {
        backService.removeAll(clz);
        plugin.clearAllCaches();
    }

    @Override
    public <T extends AAAObject> T findByName(String name, Class<T> clz) {
        T t = plugin.cachedAAAObject(name, clz);
        if (null == t) {
            t = backService.findByName(name, clz);
            if (null != t) {
                plugin.cache(t);
            }
        }
        return t;
    }

    @Override
    public Privilege findPrivilege(int level) {
        Privilege p = plugin.lookupPrivilege(level);
        if (null == p) {
            p = backService.findPrivilege(level);
            if (null != p) {
                plugin.cache(p);
            }
        }
        return p;
    }

    @Override
    public Iterable<Privilege> allPrivileges() {
        return backService.allPrivileges();
    }

    @Override
    public Iterable<Permission> allPermissions() {
        return backService.allPermissions();
    }

    @Override
    public Iterable<Role> allRoles() {
        return backService.allRoles();
    }

    @Override
    public Iterable<String> allPrivilegeNames() {
        return backService.allPrivilegeNames();
    }

    @Override
    public Iterable<String> allPermissionNames() {
        return backService.allPermissionNames();
    }

    @Override
    public Iterable<String> allRoleNames() {
        return backService.allRoleNames();
    }

    public static PersistenceServiceCache wrap(AAAPersistentService service) {
        return service instanceof PersistenceServiceCache ? (PersistenceServiceCache)service : new PersistenceServiceCache(service);
    }
}
