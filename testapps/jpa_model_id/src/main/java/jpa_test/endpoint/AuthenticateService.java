package jpa_test.endpoint;

import static org.osgl.http.H.Method.GET;
import static org.osgl.http.H.Method.POST;

import act.app.ActionContext;
import org.osgl.mvc.annotation.Action;
import org.osgl.mvc.annotation.PostAction;
import jpa_test.model.User;

public class AuthenticateService extends PublicServiceBaseV1 {

    @PostAction("login")
    public void login(String username, char[] password, ActionContext ctx, User.Dao userDao) {
        notFoundIfNot(userDao.authenticate(username, password));
        User user = userDao.findByUsername(username);
        ctx.login(user.id);
    }

    @Action(value = "logout", methods = {POST, GET})
    public void logout(ActionContext ctx) {
        ctx.logout();
    }

}
