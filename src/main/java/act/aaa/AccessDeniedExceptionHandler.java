package act.aaa;

/*-
 * #%L
 * ACT AAA Plugin
 * %%
 * Copyright (C) 2015 - 2017 ActFramework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import act.app.ActionContext;
import act.handler.builtin.controller.ActionHandlerInvoker;
import act.handler.builtin.controller.ExceptionInterceptor;
import act.view.ActForbidden;
import org.osgl.exception.AccessDeniedException;
import org.osgl.mvc.result.Result;

public class AccessDeniedExceptionHandler extends ExceptionInterceptor {

    @SuppressWarnings("unchecked")
    public AccessDeniedExceptionHandler() {
        super(0, AccessDeniedException.class);
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
    public boolean skipEvents() {
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
