package ghissues;

import act.controller.annotation.UrlContext;
import org.osgl.aaa.AAA;
import org.osgl.mvc.annotation.GetAction;

@UrlContext("28")
public class Gh28 extends TestEndpointBase {

    @GetAction
    public String test() {
        AAA.requirePrivilege(100);
        return "test";
    }

}
