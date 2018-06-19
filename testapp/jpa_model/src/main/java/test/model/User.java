package test.model;

import act.aaa.model.UserBase;
import act.aaa.model.UserLinked;
import act.db.jpa.JPADao;
import act.util.Stateless;
import org.osgl.aaa.Principal;
import org.osgl.util.S;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "user")
public class User extends UserBase implements UserLinked {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String firstName;
    public String lastName;

    @Override
    public boolean isLinkedTo(Principal user) {
        return S.eq(email, user.getName());
    }

    @Stateless
    public static class Dao extends JPADao<Long, User> {

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
