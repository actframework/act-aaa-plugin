package act.aaa;

import act.event.ActEvent;

/**
 * The Event raised when {@link AAAService} is initialized with a
 * {@link org.osgl.aaa.AuthenticationService}
 */
public class AuthenticationServiceInitialized extends ActEvent<AAAService> {
    AuthenticationServiceInitialized(AAAService aaaService) {
        super(aaaService);
    }
}
