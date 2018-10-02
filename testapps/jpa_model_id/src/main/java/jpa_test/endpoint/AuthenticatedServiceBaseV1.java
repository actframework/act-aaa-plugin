package jpa_test.endpoint;

import act.aaa.LoginUser;
import jpa_test.model.User;

public class AuthenticatedServiceBaseV1 extends ServiceBaseV1 {
    @LoginUser
    protected User me;
}
