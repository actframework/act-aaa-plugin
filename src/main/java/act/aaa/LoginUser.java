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

import act.aaa.util.LoginUserFinder;
import org.osgl.aaa.Principal;
import org.osgl.inject.annotation.InjectTag;
import org.osgl.inject.annotation.LoadValue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation specify a field or parameter should be the current
 * login user
 */
@InjectTag
@LoadValue(LoginUserFinder.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface LoginUser {
    /**
     * Specify the property name to search the user in the database using {@link Principal#getName() principal name}
     *
     * Default value: empty string.
     *
     * If not specified then it will use {@link act.aaa.AAAConfig.user#key aaa.user.key} configuration as the key
     *
     * @return user key
     */
    String key() default "";
}
