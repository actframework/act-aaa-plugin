package act.aaa;

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
        Act.app().interceptorManager().registerInterceptor(new NoAccessExceptionHandler());
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
            return aaa().authenticationService;
        }
    }

    public static class AuthorizationServiceProvider extends ActProvider<AuthorizationService> {
        @Override
        public AuthorizationService get() {
            return aaa().authorizationService;
        }
    }

    public static class AuditorProvider extends ActProvider<Auditor> {
        @Override
        public Auditor get() {
            return aaa().auditor;
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
