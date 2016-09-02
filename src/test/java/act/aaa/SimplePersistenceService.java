package act.aaa;

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
