package jpa_test.model;

import act.aaa.model.UserLinked;
import act.db.jpa.JPADao;
import act.util.SimpleBean;
import act.util.Stateless;
import org.osgl.aaa.Principal;
import org.osgl.util.S;
import jpa_test.endpoint.CustomerLinked;

import javax.persistence.*;

@Entity(name = "orders")
public class Order implements SimpleBean, UserLinked, CustomerLinked {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "agent_id")
    public User agent;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cust_id", nullable = false)
    public User customer;

    public String product;

    public int quantity;

    @Override
    public boolean isLinkedTo(Principal user) {
        String agentName = null == agent ? null : agent.email;
        String username = user.getName();
        return S.eq(username, agentName);
    }

    @Override
    public boolean isLinkedToCustomer(Principal principal) {
        String customerName = null == customer ? null : customer.email;
        String username = principal.getName();
        return S.eq(username, customerName);
    }

    @Stateless
    public static class Dao extends JPADao<Long, Order> {
    }

}
