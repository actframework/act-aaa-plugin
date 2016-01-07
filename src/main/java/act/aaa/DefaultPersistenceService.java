package act.aaa;

import act.util.DestroyableBase;
import org.osgl.aaa.*;
import org.osgl.util.C;
import org.osgl.util.E;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;

public class DefaultPersistenceService extends DestroyableBase implements AAAPersistentService {

    private ActAAAService actAAAService;

    private Map<String, Role> roles = C.newMap();
    private Map<String, Permission> permissions = C.newMap();
    private Map<String, Privilege> privileges = C.newMap();

    public DefaultPersistenceService(ActAAAService ActAAAService) {
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
            roles.put(aaaObject.getName(), (Role) aaaObject);
        } else if (aaaObject instanceof Permission) {
            permissions.put(aaaObject.getName(), (Permission) aaaObject);
        } else if (aaaObject instanceof Privilege) {
            privileges.put(aaaObject.getName(), (Privilege) aaaObject);
        } else {
            throw E.unsupport("Unknown aaa object type: %s", aaaObject.getClass());
        }
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
            return (T) roles.get(name);
        } else if (Permission.class.isAssignableFrom(aClass)) {
            return (T) permissions.get(name);
        } else if (Privilege.class.isAssignableFrom(aClass)) {
            return (T) privileges.get(name);
        } else {
            throw E.unsupport("Unknown aaa object type: %s", aClass);
        }
    }

    public Set<String> roleNames() {
        return roles.keySet();
    }

    public Set<String> privilegeNames() {
        return privileges.keySet();
    }

    public Set<String> permissionNames() {
        return permissions.keySet();
    }
}
