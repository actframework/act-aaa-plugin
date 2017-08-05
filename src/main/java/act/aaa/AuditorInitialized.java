package act.aaa;

import act.event.ActEvent;

/**
 * The Event raised when {@link AAAService} is initialized with a
 * {@link org.osgl.aaa.AuthenticationService}
 */
public class AuditorInitialized extends ActEvent<AAAService> {
    AuditorInitialized(AAAService aaaService) {
        super(aaaService);
    }
}
