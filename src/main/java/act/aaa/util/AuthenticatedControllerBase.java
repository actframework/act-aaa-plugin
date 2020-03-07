package act.aaa.util;

/*-
 * #%L
 * ACT AAA Plugin
 * %%
 * Copyright (C) 2015 - 2019 ActFramework
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

import act.aaa.LoginUser;
import act.controller.Controller;
import org.osgl.aaa.Principal;

/**
 * Base class for controller that require session has been logged in.
 *
 * Sub class can refer to {@link #me} - the logged in user instance.
 *
 * @param <T> the user type
 */
public abstract class AuthenticatedControllerBase<T extends Principal> extends Controller.Base {
    @LoginUser
    protected T me;
}
