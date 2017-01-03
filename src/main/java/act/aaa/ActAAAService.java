package act.aaa;

import act.Act;
import act.db.Dao;
import act.util.SingletonBase;
import org.osgl.$;
import org.osgl.aaa.*;
import org.osgl.aaa.impl.SimplePrincipal;
import org.osgl.cache.CacheService;
import org.osgl.util.C;
import org.osgl.util.E;
import org.osgl.util.Generics;
import org.osgl.util.S;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

public interface ActAAAService extends AuthenticationService {
    void save(Principal principal);
    void removeAllPrincipals();
    Principal findByName(String name);

    abstract class Base<USER_TYPE> extends SingletonBase implements ActAAAService {

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
         * Find a principal from data store by name.
         * @param name the principal name (could be an email or username)
         * @return the principal specified by the name
         */
        @Override
        public Principal findByName(String name) {
            USER_TYPE user = findUser(name);
            if (null == user) {
                throw new NullPointerException("Cannot find user by name: " + name);
            }
            return principalOf(user);
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
        protected Principal buildPrincipalFrom(USER_TYPE user) {
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
            return $.getProperty(cacheService, user, _userKey());
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
         * Sub class can overwrite this method to store any user data (can be serizlied
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

        private USER_TYPE findUser(String username) {
            return userDao.findOneBy(_userKey(), username);
        }

        private String _userKey() {
            return AAAConfig.user.key.get();
        }

        private void initUserType(Class<USER_TYPE> userType) {
            this.cacheService = Act.cache();
            this.userType = $.notNull(userType);
            this.userTypeIsPrincipal = Principal.class.isAssignableFrom(userType);
            this.userDao = Act.app().dbServiceManager().dao(userType);
            String userKey = userKey();
            if (S.notBlank(userKey)) {
                $.setProperty(AAAConfig.user.key, userKey, "v");
            }
        }
    }
}
