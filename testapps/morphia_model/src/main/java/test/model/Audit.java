package test.model;

import act.aaa.model.MorphiaAuditBase;
import org.mongodb.morphia.annotations.Entity;
import org.osgl.aaa.Principal;

@Entity("audit")
public class Audit extends MorphiaAuditBase {
    public Audit(Object aTarget, Principal aPrincipal, String aPermission, String aPrivilege, boolean theSuccess, String aMessage) {
        super(aTarget, aPrincipal, aPermission, aPrivilege, theSuccess, aMessage);
    }
}
