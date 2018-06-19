package test.endpoint;

import static org.osgl.http.H.Method.GET;
import static org.osgl.http.H.Method.POST;

import act.app.ActionContext;
import org.osgl.mvc.annotation.Action;
import org.osgl.mvc.annotation.PostAction;
import test.model.User;

public class AuthenticateService extends PublicServiceBaseV1 {

    @PostAction("login")
    public void login(String username, char[] password, ActionContext ctx, User.Dao userDao) {
        notFoundIfNot(userDao.authenticate(username, password));
        ctx.login(username);
    }

    @Action(value = "logout", methods = {POST, GET})
    public void logout(ActionContext ctx) {
        ctx.logout();
    }

}
