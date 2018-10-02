package jpa_test.endpoint;

import act.controller.annotation.UrlContext;
import act.db.sql.tx.Transactional;
import org.osgl.aaa.AAA;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.annotation.PostAction;
import jpa_test.model.User;

import javax.inject.Inject;

@UrlContext("users")
public class UserService extends ServiceBaseV1 {

    @Inject
    private User.Dao userDao;

    @GetAction
    public Iterable<User> list() {
        AAA.requirePermission("view-users");
        return userDao.findAll();
    }

    @PostAction
    @Transactional
    public User create(User user) {
        AAA.requirePermission("create-user");
        return userDao.save(user);
    }

}
