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
import act.app.ActionContext;
import act.app.ActionContext.PreFireSessionResolvedEvent;
import act.app.App;
import act.app.event.AppStop;
import act.app.event.SysEventId;
import act.event.*;
import act.util.DestroyableBase;
import org.osgl.$;
import org.osgl.aaa.*;
import org.osgl.aaa.impl.DumbAuditor;
import org.osgl.cache.CacheService;
import org.osgl.http.H;
import org.osgl.util.E;
import org.osgl.util.S;
import osgl.version.Version;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.*;

@Singleton
public class AAAPlugin extends DestroyableBase {

    /**
     * Defines the version of AAA plugin
     */
    public static final Version VERSION = Version.of(AAAPlugin.class);

    private ConcurrentMap<App, AAAService> services = new ConcurrentHashMap<>();

    // for roles/permissions/privileges
    private CacheService aaaObjectCache;

    // for app users (See `@LoginUser` annotation)
    private CacheService appUserCache;

    // for principals
    private CacheService principalCache;

    private Map<Integer, Privilege> privilegeLookup = new HashMap<>();

    private final int DEF_CACHE_TTL = 60 * 60 * 24;

    @Inject
    public AAAPlugin(EventBus eventBus,
                     @Named("aaa.object") CacheService aaaObjectCache,
                     @Named("aaa.principal") CacheService principalCache,
                     @Named("aaa.user") CacheService appUserCache
    ) {
        eventBus.bind(PreFireSessionResolvedEvent.class, new ActEventListenerBase<PreFireSessionResolvedEvent>() {
            @Override
            public void on(PreFireSessionResolvedEvent event) {
                ActionContext context = event.source();
                H.Session session = event.session();
                AAAService service = services.get(context.app());
                if (Act.isDev() && !service.serviceInitialized()) {
                    throw Act.getInstance(RenderInitHtml.class);
                }
                service.sessionResolved(session, context);
            }
        });
        this.aaaObjectCache = $.requireNotNull(aaaObjectCache);
        this.principalCache = $.requireNotNull(principalCache);
        this.appUserCache = $.requireNotNull(appUserCache);
    }

    @Override
    protected void releaseResources() {
        services.clear();
    }

    public void cache(AAAObject object) {
        if (object instanceof Principal) {
            principalCache.put(cacheKey(object), object, DEF_CACHE_TTL);
        } else {
            aaaObjectCache.put(cacheKey(object), object, DEF_CACHE_TTL);
            if (object instanceof Privilege) {
                Privilege p = (Privilege) object;
                privilegeLookup.put(p.getLevel(), p);
            }
        }
    }

    public void cacheUser(String key, Object user) {
        appUserCache.put(key, user, DEF_CACHE_TTL);
    }

    public <T extends AAAObject> T cachedAAAObject(String name, Class<T> clz) {
        if (Principal.class == clz || Principal.class.isAssignableFrom(clz)) {
            return principalCache.get(cacheKey(clz, name));
        }
        return aaaObjectCache.get(cacheKey(clz, name));
    }

    public <T> T cachedUser(String key) {
        return appUserCache.get(key);
    }

    public Privilege lookupPrivilege(Integer level) {
        return privilegeLookup.get(level);
    }

    public void evictAAAObject(AAAObject object) {
        if (object instanceof Principal) {
            principalCache.evict(cacheKey(object));
        } else {
            aaaObjectCache.evict(cacheKey(object));
            if (object instanceof Privilege) {
                Privilege p = (Privilege) object;
                privilegeLookup.remove(p.getLevel());
            }
        }
    }

    public void evictUser(String key) {
        appUserCache.evict(key);
    }

    public void clearAllCaches() {
        aaaObjectCache.clear();
        appUserCache.clear();
        privilegeLookup.clear();
    }

    public void clearPrincipalCache() {
        principalCache.clear();
    }

    public void clearUserCache() {
        appUserCache.clear();
    }

    public void clearUserAndPrincipalCache() {
        clearUserCache();
        clearPrincipalCache();
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

    public boolean initialized(App app) {
        AAAService aaa = services.get(app);
        return null != aaa && aaa.serviceInitialized();
    }

    public boolean isAudiorInitialized(App app) {
        AAAService aaa = services.get(app);
        if (null == aaa) {
            return false;
        }
        Auditor auditor = aaa.auditor();
        return null != auditor && auditor != DumbAuditor.INSTANCE;
    }

    private AAAService initializeAAAService(final App app, final ActAAAService appSvc) {
        AAAService svc = services.get(app);
        if (null != svc) {
            return svc;
        }
        svc = null == appSvc ? new AAAService(app) : new AAAService(app, appSvc);
        services.put(app, svc);
        EventBus eventBus = app.eventBus();
        eventBus.bind(SysEventId.STOP, new SysEventListenerBase<AppStop>("aaa-stop") {
            @Override
            public void on(AppStop event) {
                services.remove(app);
            }
        });
        return svc;
    }

    private String typeName(AAAObject object) {
        if (Principal.class.isInstance(object)) {
            return "Principal";
        } else if (Permission.class.isInstance(object)) {
            return "Permission";
        } else if (Privilege.class.isInstance(object)) {
            return "Privilege";
        } else {
            return "Role";
        }
    }

    private String typeName(Class<? extends AAAObject> clz) {
        if (Principal.class.isAssignableFrom(clz)) {
            return "Principal";
        } else if (Permission.class.isAssignableFrom(clz)) {
            return "Permission";
        } else if (Privilege.class.isAssignableFrom(clz)) {
            return "Privilege";
        } else {
            return "Role";
        }
    }

    private String cacheKey(Class<? extends AAAObject> clz, String name) {
        return cacheKey(typeName(clz), name);
    }

    private String cacheKey(AAAObject object) {
        return cacheKey(typeName(object), object.getName());
    }

    private String cacheKey(String typeName, String name) {
        return S.concat(name, "-", typeName);
    }

    public interface Listener {
        /**
         * Fired when {@link Principal} is resolved from session
         *
         * @param p
         *         the principal. Will be {@code null} if no principal found
         * @param context
         *         the current action context
         */
        void principalResolved(Principal p, ActionContext context);
    }
}
