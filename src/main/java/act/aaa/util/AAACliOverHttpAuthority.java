package act.aaa.util;

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
import act.aaa.AAAConfig;
import act.app.event.SysEventId;
import act.cli.CliOverHttpAuthority;
import org.osgl.aaa.AAA;

/**
 * AAA default implementation of {@link CliOverHttpAuthority}.
 */
public class AAACliOverHttpAuthority implements CliOverHttpAuthority {

    private boolean enabled;
    private int privilege;

    public AAACliOverHttpAuthority() {
        Act.jobManager().on(SysEventId.START, new Runnable() {
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
