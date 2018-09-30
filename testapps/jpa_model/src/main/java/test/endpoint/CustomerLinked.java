package test.endpoint;

import act.aaa.DynamicPermissionCheckHelperBase;
import org.osgl.aaa.Principal;
import org.osgl.util.C;

import java.util.List;

public interface CustomerLinked {

    boolean isLinkedToCustomer(Principal principal);

    class DynamicPermissionChecker extends DynamicPermissionCheckHelperBase<CustomerLinked> {
        @Override
        public boolean isAssociated(CustomerLinked target, Principal user) {
            return target.isLinkedToCustomer(user);
        }

        @Override
        protected List<String> permissionNames() {
            return C.list("view-my-order");
        }
    }

}
