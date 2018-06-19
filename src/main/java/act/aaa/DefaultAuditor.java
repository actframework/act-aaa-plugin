package act.aaa;

/*-
 * #%L
 * ACT AAA Plugin
 * %%
 * Copyright (C) 2015 - 2018 ActFramework
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
import act.app.App;
import act.db.Dao;
import act.job.OnAppStart;
import act.util.LogSupport;
import act.util.Stateless;
import org.osgl.aaa.Auditor;
import org.osgl.aaa.Principal;
import org.osgl.util.S;

import java.lang.reflect.Constructor;

@Stateless
public class DefaultAuditor extends LogSupport implements Auditor {

    private Constructor constructor;
    private Dao dao;
    private boolean auditFailed;

    @OnAppStart
    public void init(App app, AAAMetaInfo metaInfo) {
        auditFailed = !AAAConfig.AUDIT.get();
        if (auditFailed) {
            info("Auditing disabled");
            return;
        }
        String s = AAAConfig.AUDIT_MODEL.get();
        Class<?> auditType;
        if (S.notBlank(s)) {
            try {
                auditType = Act.appClassForName(s);
            } catch (Exception e) {
                error(e, "Error init DefaultAuditor - class[%s] not loaded", s);
                return;
            }
        } else {
            if (metaInfo.auditTypes.size() == 1) {
                auditType = metaInfo.auditTypes.iterator().next();
            } else {
                warn("Audit entity type not found. Auditing is disabled");
                return;
            }
        }
        try {
            dao = app.dbServiceManager().dao(auditType);
        } catch (Exception e) {
            error(e, "Error init DefaultAuditor - Dao not found for " + s);
            return;
        }
        try {
            constructor = auditType.getConstructor(
                    Object.class, // target
                    Principal.class, // principal
                    String.class, // permission
                    String.class, // privilege
                    boolean.class, // success
                    String.class // message
            );
        } catch (Exception e) {
            error(e, "Error init Default Auditor - proper constructor not found on " + s);
        }
    }

    @Override
    public void audit(Object target, Principal principal, String permission, String privilege, boolean success, String message) {
        if (!auditFailed && null != constructor) {
            Object record;
            try {
                record = constructor.newInstance(target, principal, permission, privilege, success, message);
            } catch (Exception e) {
                error(e, "Error log audit record");
                auditFailed = true;
                return;
            }
            try {
                dao.save(record);
            } catch (Exception e) {
                error(e, "Error log audit record");
                auditFailed = true;
            }
        }
    }
}
