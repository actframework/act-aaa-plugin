package act.aaa;

import act.ActComponent;
import act.app.ActionContext;
import act.handler.builtin.controller.ActionHandlerInvoker;
import act.handler.builtin.controller.ExceptionInterceptor;
import org.osgl.aaa.NoAccessException;
import org.osgl.mvc.result.Forbidden;
import org.osgl.mvc.result.Result;

@ActComponent
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
    protected Result internalHandle(Exception e, ActionContext actionContext) {
        return Forbidden.INSTANCE;
    }
}
