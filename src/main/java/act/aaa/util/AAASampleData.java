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
import act.apidoc.SampleData;
import act.apidoc.SampleDataCategory;
import act.apidoc.SampleDataProvider;
import act.apidoc.sampledata.EmailProvider;
import act.apidoc.sampledata.StringListProvider;
import act.apidoc.sampledata.StringListStringProvider;
import act.util.Lazy;
import org.osgl.$;
import org.osgl.aaa.*;
import org.osgl.aaa.impl.SimplePermission;
import org.osgl.aaa.impl.SimplePrincipal;
import org.osgl.aaa.impl.SimplePrivilege;
import org.osgl.aaa.impl.SimpleRole;
import org.osgl.util.C;
import org.osgl.util.N;
import org.osgl.util.S;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

public abstract class AAASampleData {
    private AAASampleData() {}

    public static class FallbackProviders {

        @Singleton
        @SampleData.Category(SampleDataCategory.ROLE)
        public static class RoleProvider extends StringListStringProvider {
        }

        @Singleton
        @SampleData.Category(SampleDataCategory.PERMISSION)
        public static class PermissionProvider extends StringListStringProvider {
        }

        @Singleton
        @SampleData.Category(SampleDataCategory.PRIVILEGE)
        public static class PrivilegeProvider extends StringListStringProvider {
        }

    }



    private abstract static class AAAStringProviderBase extends SampleDataProvider<String> {

        AAAPersistentService persistentService;

        private List<String> permissions;
        private List<String> roles;
        private List<String> privileges;

        AAAStringProviderBase() {
        }

        private AAAPersistentService persistentService() {
            if (null == persistentService) {
                persistentService = Act.getInstance(AAAPersistentService.class);
            }
            return persistentService;
        }

        protected List<String> permissions() {
            if (null == permissions) {
                permissions = C.list(persistentService().allPermissionNames());
                if (permissions.isEmpty()) {
                    permissions = C.newList();
                    FallbackProviders.PermissionProvider provider = Act.getInstance(FallbackProviders.PermissionProvider.class);
                    permissions.add(provider.get());
                    permissions.add(provider.get());
                }
            }
            return permissions;
        }

        protected List<String> privileges() {
            if (null == privileges) {
                privileges = C.list(persistentService().allPrivilegeNames());
                if (privileges.isEmpty()) {
                    privileges = C.newList();
                    FallbackProviders.PrivilegeProvider provider = Act.getInstance(FallbackProviders.PrivilegeProvider.class);
                    for (int i = 0; i < 2; ++i) {
                        String s = provider.get();
                        if (S.blank(s)) {
                            s = $.randomStr();
                        } else {
                            if (s.contains("=")) {
                                s = S.cut(s).beforeFirst("=");
                            }
                        }
                        privileges.add(s);
                    }
                }
            }
            return privileges;
        }

        protected List<String> roles() {
            if (null == roles) {
                roles = C.list(persistentService().allRoleNames());
                if (roles.isEmpty()) {
                    roles = C.newList();
                    FallbackProviders.RoleProvider provider = Act.getInstance(FallbackProviders.RoleProvider.class);
                    for (int i = 0; i < 2; ++i) {
                        String s = provider.get();
                        if (S.blank(s)) {
                            s = $.randomStr();
                        } else {
                            if (s.contains("=")) {
                                s = S.cut(s).beforeFirst("=");
                            }
                        }
                        roles.add(s);
                    }
                }
            }
            return roles;
        }

        protected static String randomMultiple(List<String> source) {
            if (null == source || source.isEmpty()) {
                return "";
            }
            int min = 1;
            int max = min + N.randInt(3);
            Set<String> set = new HashSet<>();
            for (int i = 0; i < max; ++i) {
                set.add($.random(source));
            }
            return S.join(",", set);
        }

    }

    @Singleton
    @Lazy
    @SampleData.Category(SampleDataCategory.PERMISSION)
    public static class PermissionStringProvider extends AAAStringProviderBase {
        @Override
        public String get() {
            return $.random(permissions());
        }
    }


    @Singleton
    @Lazy
    @SampleData.Category(SampleDataCategory.PERMISSIONS)
    public static class PermissionsStringProvider extends AAAStringProviderBase {
        @Override
        public String get() {
            return randomMultiple(permissions());
        }
    }

    @Singleton
    @Lazy
    @SampleData.Category(SampleDataCategory.ROLE)
    public static class RoleStringProvider extends AAAStringProviderBase {
        @Override
        public String get() {
            return $.random(roles());
        }
    }

    @Singleton
    @Lazy
    @SampleData.Category(SampleDataCategory.ROLES)
    public static class RolesStringProvider extends AAAStringProviderBase {
        @Override
        public String get() {
            return randomMultiple(roles());
        }
    }

    @Singleton
    @Lazy
    @SampleData.Category(SampleDataCategory.PRIVILEGE)
    public static class PrivilegeStringProvider extends AAAStringProviderBase {
        @Override
        public String get() {
            return $.random(privileges());
        }
    }

    @Singleton
    public static class PermissionProvider extends StringListProvider<Permission> {
        @Override
        public Permission get() {
            String name = randomStr();
            boolean dynamic = name.contains("-my-");
            return new SimplePermission(name, dynamic);
        }
    }

    @Singleton
    public static class RoleProvider extends StringListProvider<Role> {

        @Inject
        private PermissionProvider permissionProvider;

        @Override
        public Role get() {
            List<Permission> permissionList = new ArrayList<>();
            String roleName;
            String line = randomStr();
            if (line.contains("=")) {
                S.T2 t2 = S.binarySplit(line, '=');
                roleName = t2.first();
                String permissions = t2.last();
                S.List list = S.fastSplit(permissions, ",");
                for (String s : list) {
                    boolean dynamic = s.contains("-my-");
                    permissionList.add(new SimplePermission(s, dynamic));
                }
            } else {
                roleName = line;
                permissionList.add(permissionProvider.get());
                permissionList.add(permissionProvider.get());
            }
            return new SimpleRole(roleName, permissionList);
        }

    }

    @Singleton
    public static class PrivilegeProvider extends StringListProvider<Privilege> {
        @Override
        public Privilege get() {
            String name = randomStr();
            int level = N.randInt(100);
            if (name.contains("=")) {
                S.T2 t2 = S.binarySplit(name, '=');
                name = t2.first();
                String s = t2.second();
                if (N.isInt(s)) {
                    level = Integer.parseInt(s);
                }
            }
            return new SimplePrivilege(name, level);
        }
    }

    @Singleton
    public static class PrincipalProvider extends SampleDataProvider<Principal> {

        @Inject
        private EmailProvider emailProvider;

        @Inject
        private RoleProvider roleProvider;

        @Inject
        private PermissionProvider permissionProvider;

        @Inject
        private PrivilegeProvider privilegeProvider;

        @Override
        public Principal get() {
            List<Permission> permissions = new ArrayList<>();
            int max = 1 + N.randInt(5);
            for (int i = 0; i < max; ++i) {
                Permission permission = permissionProvider.get();
                if (!permissions.contains(permission)) {
                    permissions.add(permission);
                }
            }
            List<Role> roles = new ArrayList<>();
            max = 1 + N.randInt(2);
            for (int i = 0; i < max; ++i) {
                Role role = roleProvider.get();
                if (!roles.contains(role)) {
                    roles.add(role);
                }
            }
            return new SimplePrincipal(emailProvider.get(), privilegeProvider.get(), roles, permissions);
        }
    }
}
