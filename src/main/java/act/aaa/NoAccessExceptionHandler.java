package act.aaa;

import act.Act;
import act.ActComponent;
import act.app.ActionContext;
import act.handler.builtin.controller.ActionHandlerInvoker;
import act.handler.builtin.controller.ExceptionInterceptor;
import act.view.ActForbidden;
import org.osgl.aaa.NoAccessException;
import org.osgl.mvc.result.Forbidden;
import org.osgl.mvc.result.Result;

public class NoAccessExceptionHandler extends ExceptionInterceptor {

    private final boolean isDev;

    @SuppressWarnings("unchecked")
    public NoAccessExceptionHandler() {
        super(0, NoAccessException.class);
        isDev = Act.isDev();
    }

    @Override
    public void accept(ActionHandlerInvoker.Visitor visitor) {
        // do nothing
    }

    @Override
    public boolean sessionFree() {
        return true;
    }

    @Override
    protected Result internalHandle(Exception e, ActionContext actionContext) {
        return isDev ? new ActForbidden(e) : Forbidden.INSTANCE;
    }
}
