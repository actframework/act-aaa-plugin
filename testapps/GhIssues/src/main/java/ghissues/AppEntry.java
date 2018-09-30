package ghissues;

import static act.controller.Controller.Util.render;

import act.Act;
import act.app.ActionContext;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.annotation.PostAction;

@SuppressWarnings("unused")
public class AppEntry {

    @GetAction("/login")
    public void loginForm() {
        render("/login.html");
    }

    @PostAction("/login")
    public void loginUser(String username, ActionContext ctx) {
        ctx.login(username);
    }

    public static void main(String[] args) throws Exception {
        Act.start();
    }

}
