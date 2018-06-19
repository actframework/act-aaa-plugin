package test.model;

import act.aaa.model.MorphiaUserBase;
import act.aaa.model.UserLinked;
import act.db.morphia.MorphiaDao;
import act.util.Stateless;
import org.mongodb.morphia.annotations.Entity;
import org.osgl.aaa.Principal;
import org.osgl.util.S;

@Entity("user")
public class User extends MorphiaUserBase<User> implements UserLinked {

    public String firstName;
    public String lastName;

    @Override
    public boolean isLinkedTo(Principal user) {
        return S.eq(email, user.getName());
    }

    @Stateless
    public static class Dao extends MorphiaDao<User> {

        public User findByUsername(String username) {
            return findOneBy("email", username);
        }

        public boolean authenticate(String username, char[] password) {
            User user = findByUsername(username);
            if (null == user) {
                return false;
            }
            return user.verifyPassword(password);
        }

    }
}
