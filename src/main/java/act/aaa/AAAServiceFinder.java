package act.aaa;

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
import act.app.App;
import act.app.event.SysEventId;
import act.util.SubClassFinder;
import org.osgl.aaa.*;
import org.osgl.aaa.impl.DumbAuditor;

import javax.inject.Inject;

@SuppressWarnings("unused")
public class AAAServiceFinder<T> {

    private App app;

    @Inject
    public AAAServiceFinder(App app) {
        this.app = app;
    }

    @SubClassFinder(callOn = SysEventId.PRE_START)
    public void foundActAAAService(Class<ActAAAService> serviceType) {
        ActAAAService service = app.getInstance(serviceType);
        plugin().buildService(app, service);
    }

    @SubClassFinder(callOn = SysEventId.PRE_START)
    public void foundAuditorService(Class<Auditor> auditorClass) {
        if (DumbAuditor.class.equals(auditorClass)) {
            return;
        }
        Auditor auditor = app.getInstance(auditorClass);
        plugin().buildService(app, auditor);
    }

    @SubClassFinder(callOn = SysEventId.PRE_START)
    public void foundAuthenticationService(Class<AuthenticationService> serviceType) {
        if (ActAAAService.class.isAssignableFrom(serviceType)) {
            return;
        }
        AuthenticationService service = app.getInstance(serviceType);
        plugin().buildService(app, service);
    }

    @SubClassFinder(callOn = SysEventId.PRE_START)
    public void foundAuthorizationService(Class<AuthorizationService> serviceType) {
        AuthorizationService service = app.getInstance(serviceType);
        plugin().buildService(app, service);
    }

    @SubClassFinder(callOn = SysEventId.PRE_START)
    public void foundDynamicPermissionCheckHelper(final Class<DynamicPermissionCheckHelperBase> target) {
        DynamicPermissionCheckHelperBase helper = app.getInstance(target);
        AAA.registerDynamicPermissionChecker(helper, helper.getTargetClass());
    }

    @SubClassFinder
    public void handleFound(Class<AAAPersistentService> serviceType) {
        if (DefaultPersistentService.class.equals(serviceType)) {
            // DefaultPersistentService is not aimed to be used for dependency injection
            // however subclass of it might be implemented by app developer
            return;
        }
        AAAPersistentService service = app.getInstance(serviceType);
        plugin().buildService(app, service);
    }

    private AAAPlugin plugin() {
        return Act.getInstance(AAAPlugin.class);
    }

}
