package act.aaa;

import act.event.ActEvent;

/**
 * The Event raised when {@link AAAService} is initialized with a
 * {@link org.osgl.aaa.AAAPersistentService}
 */
public class AAAPersistenceServiceInitialized extends ActEvent<AAAService> {
    AAAPersistenceServiceInitialized(AAAService aaaService) {
        super(aaaService);
    }
}
