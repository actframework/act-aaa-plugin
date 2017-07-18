package act.aaa;

import act.event.ActEvent;
import org.osgl.aaa.Principal;

/**
 * Triggered when AAA service has resolved the principal from an incoming session
 */
public class PrincipalResolved extends ActEvent<Principal> {
    public PrincipalResolved(Principal source) {
        super(source);
    }
}
