package test.model;

import act.aaa.model.UserLinked;
import act.db.morphia.MorphiaAdaptiveRecord;
import act.db.morphia.MorphiaDao;
import act.util.Stateless;
import org.mongodb.morphia.annotations.Entity;
import org.osgl.aaa.Principal;
import org.osgl.util.S;
import test.endpoint.CustomerLinked;

@Entity("order")
public class Order extends MorphiaAdaptiveRecord<Order> implements UserLinked, CustomerLinked {

    public String agent;

    public String customer;

    public String product;

    public int quantity;

    @Override
    public boolean isLinkedTo(Principal user) {
        String username = user.getName();
        return S.eq(username, agent);
    }

    @Override
    public boolean isLinkedToCustomer(Principal principal) {
        String username = principal.getName();
        return S.eq(username, customer);
    }

    @Stateless
    public static class Dao extends MorphiaDao<Order> {
    }

}
