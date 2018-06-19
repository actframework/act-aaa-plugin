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
import act.aaa.event.AAAInitialized;
import act.aaa.util.AAAAdaptor;
import act.app.App;
import act.app.event.SysEventId;
import act.event.ActEventListenerBase;
import act.job.OnAppStart;
import act.util.Stateless;
import act.util.SubClassFinder;
import org.osgl.aaa.*;
import org.osgl.aaa.impl.DumbAuditor;
import org.osgl.util.E;

import java.lang.reflect.Method;
import java.util.EventObject;
import javax.inject.Inject;

@SuppressWarnings("unused")
@Stateless
public class AAAServiceFinder<T> {

    private App app;

    @Inject
    public AAAServiceFinder(App app) {
        this.app = app;
    }

    @SubClassFinder(callOn = SysEventId.PRE_START)
    public void foundActAAAService(Class<? extends ActAAAService> serviceType) {
        if (AAAAdaptor.class == serviceType) {
            return;
        }
        ActAAAService service = app.getInstance(serviceType);
        plugin().buildService(app, service);
    }

    @SubClassFinder(callOn = SysEventId.PRE_START)
    public void foundAuditorService(Class<Auditor> auditorClass) {
        if (DumbAuditor.class.equals(auditorClass) || DefaultAuditor.class.getName().equals(auditorClass.getName())) {
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
        final DynamicPermissionCheckHelperBase helper = app.getInstance(target);
        app.eventBus().bind(AAAInitialized.class, new ActEventListenerBase() {
            @Override
            public void on(EventObject event) throws Exception {
                AAA.registerDynamicPermissionChecker(helper, helper.getTargetClass());
            }
        });
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

    @OnAppStart
    public void ensureAAAIntegration(App app, AAAMetaInfo metaInfo) {
        try {
            if (plugin().initialized(app)) {
                ensureAuditor();
                return;
            }
            final Class userType = metaInfo.principalEntityType;
            E.invalidConfigurationIf(null == userType, "AAA initialization failed: cannot determine Principal entity type");
            final Method passwordVerifier = metaInfo.passwordVerifier;
            E.invalidConfigurationIf(null == passwordVerifier, "AAA initialization failed: cannot determine password verifier method");
            Class<?> passwordType = passwordVerifier.getParameterTypes()[0];
            final boolean passwordTypeIsStr = passwordType == String.class;
            final boolean passwordTypeIsCharArray = !passwordTypeIsStr && passwordType == char[].class;

            final Method roleProviderMethod = metaInfo.roleProvider;
            final Method permissionProviderMethod = metaInfo.permissionProvider;
            final Method privilegeProviderMethod = metaInfo.privilegeProvider;

            ActAAAService.Base service = new AAAAdaptor(userType, passwordTypeIsCharArray, passwordVerifier, roleProviderMethod, permissionProviderMethod, privilegeProviderMethod);
            plugin().buildService(app, service);
            ensureAuditor();
        } finally {
            app.eventBus().trigger(new AAAInitialized());
        }
    }

    private AAAPlugin plugin() {
        return Act.getInstance(AAAPlugin.class);
    }

    private void ensureAuditor() {
        if (!plugin().isAudiorInitialized(app)) {
            plugin().buildService(app, app.getInstance(DefaultAuditor.class));
        }
    }

}
