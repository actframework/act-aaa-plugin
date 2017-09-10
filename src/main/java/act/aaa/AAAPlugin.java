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

import act.Destroyable;
import act.app.ActionContext;
import act.app.App;
import act.app.event.AppEventId;
import act.app.event.AppStop;
import act.event.AppEventListenerBase;
import act.event.EventBus;
import act.util.SessionManager;
import org.osgl.aaa.*;
import org.osgl.bootstrap.Version;
import org.osgl.http.H;
import org.osgl.util.E;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AAAPlugin extends SessionManager.Listener implements Destroyable {

    /**
     * Defines the version of AAA plugin
     */
    public static final Version VERSION = Version.of(AAAPlugin.class);

    private ConcurrentMap<App, AAAService> services = new ConcurrentHashMap<App, AAAService>();

    @Override
    protected void releaseResources() {
        services.clear();
    }

    public void buildService(App app, ActAAAService service) {
        AAAService aaa = initializeAAAService(app, service);
        // we need to check if persistent service is already
        // provisioned with buildService(App, AAAPersistentService) call
        if (null == aaa.persistentService()) {
            aaa.persistentService(new DefaultPersistentService(service));
            aaa.persistentService();
        }
        // we need to check if authentication service is already
        // provisioned with buildService(App, AuthenticationService) call
        if (null == aaa.authenticationService()) {
            aaa.authenticationService(service);
        }
    }

    public void buildService(App app, AuthenticationService service) {
        AAAService aaa = initializeAAAService(app, null);
        aaa.authenticationService(service);
    }

    public void buildService(App app, AuthorizationService service) {
        AAAService aaa = initializeAAAService(app, null);
        aaa.authorizationService(service);
    }

    public void buildService(App app, AAAPersistentService service) {
        E.NPE(service);
        AAAService aaa = initializeAAAService(app, null);
        aaa.persistentService(service);
    }

    public void buildService(App app, Auditor auditor) {
        AAAService aaa = initializeAAAService(app, null);
        aaa.auditor(auditor);
    }

    private AAAService initializeAAAService(final App app, final ActAAAService appSvc) {
        AAAService svc = services.get(app);
        if (null != svc) {
            return svc;
        }
        svc = null == appSvc ? new AAAService(app) : new AAAService(app, appSvc);
        services.put(app, svc);
        EventBus eventBus = app.eventBus();
        eventBus.bind(AppEventId.STOP, new AppEventListenerBase<AppStop>("aaa-stop") {
            @Override
            public void on(AppStop event) {
                services.remove(app);
            }
        });
        return svc;
    }

    @Override
    public void sessionResolved(H.Session session, ActionContext context) {
        AAAService service = services.get(context.app());
        service.sessionResolved(session, context);
    }

    public interface Listener {
        /**
         * Fired when {@link Principal} is resolved from session
         *
         * @param p       the principal. Will be {@code null} if no principal found
         * @param context the current action context
         */
        void principalResolved(Principal p, ActionContext context);
    }
}
