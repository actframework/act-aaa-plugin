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

import static act.app.conf.AutoConfigPlugin.loadPluginAutoConfig;

import act.Act;
import act.app.App;
import act.app.conf.AutoConfig;
import act.plugin.AppServicePlugin;
import org.osgl.$;
import org.osgl.aaa.AAA;
import org.osgl.aaa.Principal;
import org.osgl.util.Const;
import org.osgl.util.S;

@AutoConfig("aaa")
public class AAAConfig extends AppServicePlugin {

    public static final Const<String> PRINCIPAL_MODEL = $.constant();
    public static final Const<String> AUDIT_MODEL = $.constant();
    public static final Const<Boolean> AUDIT = $.constant(true);
    public static final Const<String> PASSWORD_VERIFIER = $.constant();
    public static final Const<String> ROLE_PROVIDER = $.constant();
    public static final Const<String> PERMISSION_PROVIDER = $.constant();
    public static final Const<String> PRIVILEGE_PROVIDER = $.constant();

    public static final class ddl {
        /**
         * `aaa.ddl.create`
         *
         * Disable/enable create DDL for roles/permissions/privileges
         *
         * Default value: `true`
         */
        public static boolean create = true;

        /**
         * `aaa.ddl.update`
         *
         * Disable/enable update DDL for roles/permissions/privileges
         *
         * Default value: `true` when app running in {@link act.Act.Mode#DEV dev mode} or `false` otherwise
         */
        public static Boolean update = Act.isDev();

        /**
         * `aaa.ddl.delete`
         *
         * Disable/enable delete DDL for roles/permissions/privileges
         *
         * Default value: `false`
         */
        public static boolean delete = false;

        public static final class principal {
            /**
             * `aaa.ddl.principal.create`
             *
             * Disable/enable create DDL for principal
             *
             * Default value: `false`
             */
            public static boolean create = false;

            /**
             * `aaa.ddl.principal.update`
             *
             * Disable/enable update DDL for principal
             *
             * Default value: `false`
             */
            public static Boolean update = false;

            /**
             * `aaa.ddl.principal.delete`
             *
             * Disable/enable delete DDL for principal
             *
             * Default value: `false`
             */
            public static boolean delete = false;
        }
    }

    /**
     * `aaa.loginUrl`
     *
     * Specify the login URL
     */
    public static String loginUrl = null;

    public static final class user {

        public static final String DEF_USER_KEY = "email";

        /**
         * `aaa.user.key`
         *
         * Configure the key to search the user by {@link Principal#getName() name of the principal logged in}
         *
         * Default value: `email`
         */
        public static final Const<String> key = $.constant(DEF_USER_KEY);
    }

    public static final class cliOverHttp {
        /**
         * `aaa.cliOverHttp.authorization`
         *
         * When set to `true` CliOverHttp request will be authorized
         *
         * Default value: `true`
         */
        public static final Const<Boolean> authorization = $.constant(true);

        /**
         * `aaa.cliOverHttp.privilege`
         *
         * Configure the required privilege when `aaa.cliOverHttp.authorization` is enabled
         *
         * Default value: {@link AAA#SUPER_USER}
         */
        public static final Const<Integer> privilege = $.constant(AAA.SUPER_USER);
    }

    @Override
    protected void applyTo(App app) {
        loadPluginAutoConfig(AAAConfig.class, app);
        loadPluginAutoConfig(AAAService.class, app);
        ensureDDL();
        ensureLoginUrl(app);
    }

    private void ensureDDL() {
        if (null == ddl.update) {
            ddl.update = Act.isDev();
        }
    }

    private void ensureLoginUrl(final App app) {
        app.jobManager().beforeAppStart(new Runnable() {
            @Override
            public void run() {
                if (S.notBlank(loginUrl)) {
                    return;
                }
                loginUrl = app.config().loginUrl();
            }
        });
    }
}
