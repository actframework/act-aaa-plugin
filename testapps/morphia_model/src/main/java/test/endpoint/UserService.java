package test.endpoint;

import act.controller.annotation.UrlContext;
import org.osgl.aaa.AAA;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.annotation.PostAction;
import test.model.User;

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
    public User create(User user) {
        AAA.requirePermission("create-user");
        return userDao.save(user);
    }

}
