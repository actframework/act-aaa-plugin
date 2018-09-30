package ghissues;

import act.aaa.model.UserBase;
import act.db.jpa.JPADao;
import act.util.Stateless;
import org.osgl.aaa.NoAuthentication;
import org.osgl.mvc.annotation.PostAction;

import javax.persistence.*;

@Entity
public class User extends UserBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Stateless
    public static class Dao extends JPADao<Integer, User> {
        @PostAction("/users")
        @NoAuthentication
        public User create(User user) {
            return save(user);
        }
    }
}
