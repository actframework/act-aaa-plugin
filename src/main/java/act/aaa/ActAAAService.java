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

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

public interface ActAAAService extends AuthenticationService {
    void save(Principal principal);
    void removeAllPrincipals();
    Principal findByName(String name);

    abstract class Base<USER_TYPE> extends SingletonBase implements ActAAAService {

        protected Class<USER_TYPE> userType;
        private boolean userTypeIsPrincipal;
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

        @Override
        public void save(Principal principal) {
            throw E.unsupport();
        }

        @Override
        public void removeAllPrincipals() {
            throw E.unsupport();
        }

        @Override
        public Principal findByName(String name) {
            USER_TYPE user = findUser(name);
            return principalOf(user);
        }

        @Override
        public Principal authenticate(String username, String password) {
            USER_TYPE user = findUser(username);
            return null == user ? null : verifyPassword(user, password.toCharArray()) ? principalOf(user) : null;
        }

        @Override
        public Principal authenticate(String username, char[] password) {
            USER_TYPE user = findUser(username);
            return null == user ? null : verifyPassword(user, password) ? principalOf(user) : null;
        }

        protected Principal principalOf(USER_TYPE user) {
            if (userTypeIsPrincipal) {
                return (Principal) user;
            }
            return buildPrincipalFrom(user);
        }

        protected Principal buildPrincipalFrom(USER_TYPE user) {
            SimplePrincipal.Builder pb = new SimplePrincipal.Builder(nameOf(user));
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

        protected String username(USER_TYPE user) {
            return $.getProperty(cacheService, user, userKey());
        }

        @Deprecated
        protected String nameOf(USER_TYPE user) {
            return username(user);
        }

        protected Integer privilegeOf(USER_TYPE user) {
            return null;
        }

        protected Collection<String> rolesOf(USER_TYPE user) {
            return C.list();
        }

        protected Collection<String> permissionsOf(USER_TYPE user) {
            return C.list();
        }

        protected void setPrincipalProperties(Principal principal, USER_TYPE user) {}

        protected abstract boolean verifyPassword(USER_TYPE user, char[] password);

        private USER_TYPE findUser(String username) {
            return userDao.findOneBy(userKey(), username);
        }

        private String userKey() {
            return AAAConfig.user.key.get();
        }

        private void initUserType(Class<USER_TYPE> userType) {
            this.userType = $.notNull(userType);
            this.userTypeIsPrincipal = Principal.class.isAssignableFrom(userType);
            this.userDao = Act.app().dbServiceManager().dao(userType);
        }
    }
}
