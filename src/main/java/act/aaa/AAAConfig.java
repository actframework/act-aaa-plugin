package act.aaa;

import act.Act;
import act.app.App;
import act.app.conf.AutoConfig;
import act.app.conf.AutoConfigPlugin;
import act.plugin.AppServicePlugin;

@AutoConfig("aaa")
public class AAAConfig extends AppServicePlugin {

    public static boolean alwaysAuthenticate = true;
    public static String loginUrl = "/login";

    public static class ddl {
        public static boolean create = true;
        public static Boolean update = null;
        public static boolean delete = false;
        public static class principal {
            public static boolean create = false;
            public static Boolean update = false;
            public static boolean delete = false;
        }
    }

    @Override
    protected void applyTo(App app) {
        AutoConfigPlugin.loadPluginAutoConfig(AAAConfig.class, app);
        ensureDDL();
    }

    private void ensureDDL() {
        if (null == ddl.update) {
            ddl.update = Act.isDev();
        }
    }
}
