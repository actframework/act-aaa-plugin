package jpa_test.model;

import act.aaa.model.AuditBase;
import org.osgl.aaa.Principal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "audit")
public class Audit extends AuditBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Audit() {
    }

    public Audit(Object aTarget, Principal aPrincipal, String aPermission, String aPrivilege, boolean theSuccess, String aMessage) {
        super(aTarget, aPrincipal, aPermission, aPrivilege, theSuccess, aMessage);
    }
}
