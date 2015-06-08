package act.aaa;

import act.app.App;
import act.app.AppByteCodeScanner;
import act.util.SubTypeFinder;
import org.osgl._;

import java.util.Map;
import java.util.Set;

public class AuthorizationServiceFinder extends SubTypeFinder {
    protected AuthorizationServiceFinder() {
        super(superType, foundHandler);
    }
}
