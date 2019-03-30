package act.aaa.util;

import act.aaa.LoginUser;
import act.aaa.model.UserBase;
import act.controller.Controller;

/**
 * Base class for controller that require session has been logged in.
 *
 * Sub class can refer to {@link #me} - the logged in user instance.
 *
 * @param <T> the user type
 */
public abstract class AuthenticatedControllerBase<T extends UserBase> extends Controller.Base {
    @LoginUser
    protected T me;
}
