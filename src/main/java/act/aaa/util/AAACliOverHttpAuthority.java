package act.aaa.util;

import act.Act;
import act.aaa.AAAConfig;
import act.app.event.AppEventId;
import act.cli.CliOverHttpAuthority;
import org.osgl.aaa.AAA;

/**
 * AAA default implementation of {@link CliOverHttpAuthority}.
 */
public class AAACliOverHttpAuthority implements CliOverHttpAuthority {

    private boolean enabled;
    private int privilege;

    public AAACliOverHttpAuthority() {
        Act.jobManager().on(AppEventId.START, new Runnable() {
            @Override
            public void run() {
                delayedInit();
            }
        });
    }

    @Override
    public void authorize() {
        if (enabled) {
            AAA.requirePrivilege(privilege);
        }
    }

    private void delayedInit() {
        enabled = AAAConfig.cliOverHttp.authorization.get();
        privilege = AAAConfig.cliOverHttp.privilege.get();
    }

}
