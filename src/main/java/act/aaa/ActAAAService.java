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

import act.Act;
import act.app.event.SysEventId;
import act.db.Dao;
import act.util.LogSupport;
import org.osgl.$;
import org.osgl.aaa.*;
import org.osgl.aaa.impl.SimplePrincipal;
import org.osgl.cache.CacheService;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.Generics;
import org.osgl.util.S;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;

public interface ActAAAService extends AuthenticationService {
    void save(Principal principal);
    void removeAllPrincipals();
    Principal findByName(String name);

    abstract class Base<USER_TYPE> extends LogSupport implements ActAAAService {

        /**
         * The user model class
         */
        protected Class<USER_TYPE> userType;

        private boolean userTypeIsPrincipal;

        /**
         * The user model DAO class
         */
        protected Dao<?, USER_TYPE, ?> userDao;

        @Inject
        private CacheService cacheService;

        @Inject
        private Provider<AAAPersistentService> persistentServiceProvider;


        public Base() {
            List<Type> typeParams = Generics.typeParamImplementations(getClass(), Base.class);
            initUserType((Class<USER_TYPE>) typeParams.get(0));
        }

        public Base(Class<USER_TYPE> userType) {
            initUserType(userType);
        }

        /**
         * Save an new principal into data store
         *
         * Called when there are principals defined in the `_act.yml` file
         *
         * The default implementation is to throw out UnsupportedException
         *
         * @param principal the principal instance defined in `_act.yml` file
         */
        @Override
        public void save(Principal principal) {
            throw E.unsupport();
        }

        /**
         * Remove all principals from data store.
         *
         * Called when loading yaml content and the `aaa.ddl.delete` configuration
         * is enabled.
         *
         * The default implementation is to throw out UnsupportedException
         */
        @Override
        public void removeAllPrincipals() {
            throw E.unsupport();
        }

        /**
         * Find a principal from data store by user identifier.
         * @param identifier
         *      the user identifier, could be `id`, `username`, `email`,
         *      depends on how {@link AAAConfig.user#key} is configured.
         * @return the principal specified by the identifier
         */
        @Override
        public Principal findByName(String identifier) {
            USER_TYPE user = findUser(identifier);
            return null == user ? null : principalOf(user);
        }

        /**
         * Authenticate a username against a password, return a {@link Principal}
         * instance if authenticated, or `null` if failed to authenticate.
         *
         * Sub class can (but not required to) overwrite this method
         *
         * @param username the username
         * @param password the password
         * @return the principal matches the username/password pair or `null` if failed to authenticate
         */
        @Override
        public Principal authenticate(String username, String password) {
            USER_TYPE user = findUser(username);
            return null == user ? null : verifyPassword(user, password.toCharArray()) ? principalOf(user) : null;
        }

        /**
         * Authenticate a username against a password, return a {@link Principal} instance
         * if authenticated, or `null` if failed to authenticate.
         *
         * Sub class can (but not required to) overwrite this method
         *
         * @param username the username
         * @param password the password
         * @return the principal matches the username/password pair or `null` if failed to authenticate
         */
        @Override
        public Principal authenticate(String username, char[] password) {
            USER_TYPE user = findUser(username);
            return null == user ? null : verifyPassword(user, password) ? principalOf(user) : null;
        }

        /**
         * Return the key name used to find out the user entity from data store.
         *
         * The default implementation returns `null`, meaning AAA will use the
         * {@link act.aaa.AAAConfig.user#key aaa.user.key} configuration as
         * the userKey
         *
         * @return the key name to get the user from data store
         */
        protected String userKey() {
            return null;
        }

        /**
         * Return the username field name used to find out the user entity from data store.
         *
         * The default implementation returns `null`, meaning AAA will use the
         * {@link act.aaa.AAAConfig.user#username aaa.user.username} configuration as
         * the usernameField
         *
         * @return the username field name to get the user from data store
         */
        protected String usernameField() {
            return null;
        }

        /**
         * Get {@link Principal} instance from a user instance.
         * @param user the user instance
         * @return the principal instance
         */
        protected Principal principalOf(USER_TYPE user) {
            if (userTypeIsPrincipal) {
                return (Principal) user;
            }
            return buildPrincipalFrom(user);
        }

        /**
         * Get the {@link Principal} from the user entity.
         *
         * This method relies on the implementation of
         * - {@link #privilegeOf(Object)}
         * - {@link #rolesOf(Object)}
         * - {@link #permissionsOf(Object)}
         *
         * App developer can also overwrite this method though
         * not recommended to do that
         *
         * @param user the user entity instance
         * @return the principal corresponding to the user entity
         */
        private Principal buildPrincipalFrom(USER_TYPE user) {
            if (user instanceof Principal) {
                return (Principal) user;
            }
            SimplePrincipal.Builder pb = new SimplePrincipal.Builder(username(user));
            AAAPersistentService store = persistentServiceProvider.get();
            Integer I = privilegeOf(user);
            if (null != I) {
                pb.grantPrivilege(store.findPrivilege(I));
            }
            for (String role: rolesOf(user)) {
                pb.grantRole(store.findByName(role, Role.class));
            }
            for (String perm: permissionsOf(user)) {
                pb.grantPermission(store.findByName(perm, Permission.class));
            }
            Principal principal = pb.toPrincipal();
            if ("id".equals(AAAConfig.user.key.get())) {
                principal.setProperty("id", S.string($.getProperty(user, "id")));
            }
            setPrincipalProperties(principal, user);
            return principal;
        }

        /**
         * Get username of the user passed in.
         *
         * This method will use reflection to find the username by {@link #_userKey()}. However sub class
         * can overwrite this method to provide fast implementation
         *
         * @param user the user instance
         * @return the username of the user
         */
        protected String username(USER_TYPE user) {
            return $.getProperty(cacheService, user, _usernameField());
        }

        /**
         * Get username of the user passed in
         *
         * This method is deprecated. Please use {@link #username(Object)} instead
         *
         * @param user the user instance
         * @return the username of the user
         */
        @Deprecated
        protected String nameOf(USER_TYPE user) {
            return username(user);
        }

        /**
         * Returns the privilege level of a user.
         *
         * If not overwritten by sub class, this method will return `null`, meaning there is no
         * privilege granted to the user
         *
         * @param user the user instance
         * @return the privilege level
         */
        protected Integer privilegeOf(USER_TYPE user) {
            return null;
        }


        /**
         * Returns role names of specified user
         *
         * If not overwritten by sub class, this method returns an empty set
         *
         * @param user the user instance
         * @return name of roles granted to the user
         */
        protected Set<String> rolesOf(USER_TYPE user) {
            return C.set();
        }

        /**
         * Returns direct permission names of specified user
         *
         * If not overwritten by sub class, this method returns an empty set. If sub class
         * overwrite this method, it shall not return permissions inferred by roles
         *
         * @param user the user instance
         * @return name of roles granted to the user
         */
        protected Set<String> permissionsOf(USER_TYPE user) {
            return C.set();
        }

        /**
         * Sub class can overwrite this method to store any user data (can be serialised
         * to a String) into principal's property. For example user's account ID
         *
         * Default implementation is empty
         *
         * @param principal the principal instance
         * @param user the user instance
         */
        protected void setPrincipalProperties(Principal principal, USER_TYPE user) {}

        /**
         * Sub class must overwrite this method to implement password verification logic.
         *
         * Normally it should be something like:
         *
         * ```java
         * return Act.crypto().verifyPassword(password, user.getPassword());
         * ```
         *
         * @param user the user instance
         * @param password password supplied
         * @return `true` if password matches or `false` otherwise
         */
        protected abstract boolean verifyPassword(USER_TYPE user, char[] password);

        protected final USER_TYPE findUser(String identifier) {
            if (identifier.contains(":")) {
                String field = S.beforeFirst(identifier, ":");
                String value = S.afterFirst(identifier, ":");
                return findUser(field, value);
            }
            return findUser(_userKey(), identifier);
        }

        /**
         * Sub class can overwrite this method to return a User entity by
         * field name and field value.
         *
         * @param key
         *      the field name, e.g. "email" or "username" etc
         * @param value
         *      the field value
         * @return
         *      A user entity
         */
        protected USER_TYPE findUser(String key, String value) {
            if ("id".equals(key)) {
                Object id = $.convert(value).to(userDao.idType());
                Dao dao = userDao;
                return (USER_TYPE) dao.findById(id);
            } else {
                return userDao.findOneBy(key, value);
            }
        }

        private String _userKey() {
            return AAAConfig.user.key.get();
        }

        private String _usernameField() {
            return AAAConfig.user.username.get();
        }

        protected void initUserType(Class<USER_TYPE> userType) {
            this.cacheService = Act.cache();
            this.userType = $.requireNotNull(userType);
            this.userTypeIsPrincipal = Principal.class.isAssignableFrom(userType);
            String userKey = userKey();
            if (S.notBlank(userKey)) {
                $.setProperty(AAAConfig.user.key, userKey, "v");
            }
            initUserDao(userType);
        }

        protected void initUserDao(final Class<USER_TYPE> userType) {
            Act.app().jobManager().on(SysEventId.DB_SVC_LOADED, new Runnable() {
                @Override
                public void run() {
                    ActAAAService.Base.this.userDao = Act.app().dbServiceManager().dao(userType);
                }
            });
        }
    }
}
