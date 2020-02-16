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

import act.Act;
import act.inject.ActProvider;
import act.job.OnAppStart;
import org.osgl.aaa.*;

/**
 * Define DI bindings
 */
public class AAAModule {

    @OnAppStart
    public static void registerExceptionHandler() {
        Act.app().interceptorManager().registerInterceptor(new AccessDeniedExceptionHandler());
    }

    public static class AAAPersistentServiceProvider extends ActProvider<AAAPersistentService> {
        @Override
        public AAAPersistentService get() {
            return aaa().persistentService();
        }
    }

    public static class AuthenticationServiceProvider extends ActProvider<AuthenticationService> {
        @Override
        public AuthenticationService get() {
            return aaa().authenticationService();
        }
    }

    public static class AuthorizationServiceProvider extends ActProvider<AuthorizationService> {
        @Override
        public AuthorizationService get() {
            return aaa().authorizationService();
        }
    }

    public static class AuditorProvider extends ActProvider<Auditor> {
        @Override
        public Auditor get() {
            return aaa().auditor();
        }
    }

    public static class AAAContextProvider extends ActProvider<AAAContext> {
        @Override
        public AAAContext get() {
            return AAA.context();
        }
    }

    private static AAAService aaa() {
        return Act.getInstance(AAAService.class);
    }

}
