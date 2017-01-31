package act.aaa;

import act.app.ActionContext;
import act.handler.builtin.controller.ActionHandlerInvoker;
import act.handler.builtin.controller.ExceptionInterceptor;
import act.view.ActForbidden;
import org.osgl.aaa.NoAccessException;
import org.osgl.mvc.result.Result;

public class NoAccessExceptionHandler extends ExceptionInterceptor {

    @SuppressWarnings("unchecked")
    public NoAccessExceptionHandler() {
        super(0, NoAccessException.class);
    }

    @Override
    public void accept(ActionHandlerInvoker.Visitor visitor) {
        // do nothing
    }

    @Override
    public boolean express() {
        return true;
    }

    @Override
    public boolean sessionFree() {
        return true;
    }

    @Override
    protected Result internalHandle(Exception e, ActionContext actionContext) {
        return ActForbidden.create(e);
    }
}
