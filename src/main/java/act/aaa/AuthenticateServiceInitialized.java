package act.aaa;

import act.event.ActEvent;

/**
 * The Event raised when {@link AAAService} is initialized with a
 * {@link org.osgl.aaa.AuthenticationService}
 */
public class AuthenticateServiceInitialized extends ActEvent<AAAService> {
    AuthenticateServiceInitialized(AAAService aaaService) {
        super(aaaService);
    }
}
