package act.aaa;

import act.Act;
import act.inject.ActProvider;
import org.osgl.aaa.AAAPersistentService;
import org.osgl.aaa.Auditor;
import org.osgl.aaa.AuthenticationService;
import org.osgl.aaa.AuthorizationService;

/**
 * Define DI bindings
 */
public class AAAModule {

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

    private static AAAService aaa() {
        return Act.getInstance(AAAService.class);
    }

}
