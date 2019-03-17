package jpa_test.endpoint;

import act.controller.annotation.UrlContext;
import act.db.DbBind;
import org.osgl.aaa.AAA;
import org.osgl.mvc.annotation.GetAction;
import jpa_test.model.Order;
import jpa_test.model.User;

@UrlContext("my")
public class MyService extends AuthenticatedServiceBaseV1 {

    @GetAction
    public User myProfile() {
        AAA.requirePermission(me, "view-my-profile");
        return me;
    }

    @GetAction("orders/{order}")
    public Order myOrder(@DbBind Order order) {
        AAA.requirePermission(order, "view-my-order");
        return order;
    }

}
