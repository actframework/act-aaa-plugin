package test.endpoint;

import act.controller.annotation.UrlContext;
import act.db.DbBind;
import act.db.sql.tx.Transactional;
import org.osgl.aaa.AAA;
import org.osgl.mvc.annotation.GetAction;
import org.osgl.mvc.annotation.PostAction;
import test.model.Order;

import javax.inject.Inject;

@UrlContext("orders")
public class OrderService extends AuthenticatedServiceBaseV1 {

    @Inject
    private Order.Dao orderDao;

    @GetAction
    public Iterable<Order> list() {
        AAA.requirePrivilege(AAA.SUPER_USER);
        return orderDao.findAll();
    }

    @GetAction("{order}")
    public Order get(@DbBind Order order) {
        AAA.requirePermission(order, "view-orders");
        return order;
    }

    @PostAction
    @Transactional
    public Order create(Order order) {
        AAA.requirePermission("create-order");
        order.agent = me;
        return orderDao.save(order);
    }

}
