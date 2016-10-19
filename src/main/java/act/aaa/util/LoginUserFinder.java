package act.aaa.util;

import act.aaa.AAAConfig;
import act.app.App;
import act.db.Dao;
import act.inject.param.ParamValueLoaderService;
import org.osgl.aaa.AAA;
import org.osgl.aaa.AAAContext;
import org.osgl.aaa.Principal;
import org.osgl.inject.ValueLoader;
import org.osgl.util.S;

/**
 * Load user from logged in principal
 */
public class LoginUserFinder extends ValueLoader.Base {

    public static final String KEY_USER_KEY = "key";

    private String bindName;
    private Dao dao;
    private String querySpec;
    private Class<?> rawType;

    @Override
    public Object get() {
        AAAContext aaaContext = AAA.context();
        if (null != aaaContext) {
            Principal principal = aaaContext.getCurrentPrincipal();
            if (null != principal) {
                return dao.findOneBy(querySpec, principal.getName());
            }
        }
        return null;
    }

    @Override
    protected void initialized() {
        App app = App.instance();

        rawType = spec.rawType();
        dao = app.dbServiceManager().dao(rawType);

        bindName = ParamValueLoaderService.bindName(spec);
        querySpec = S.string(options.get(KEY_USER_KEY));
        if (S.blank(querySpec)) {
            querySpec = AAAConfig.user.key.get();
        }
    }
}
