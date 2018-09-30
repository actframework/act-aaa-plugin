package test.endpoint;

import act.aaa.LoginUser;
import test.model.User;

public class AuthenticatedServiceBaseV1 extends ServiceBaseV1 {
    @LoginUser
    protected User me;
}
