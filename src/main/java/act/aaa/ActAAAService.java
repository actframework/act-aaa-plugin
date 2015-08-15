package act.aaa;

import org.osgl.aaa.AuthenticationService;
import org.osgl.aaa.Principal;

public interface ActAAAService extends AuthenticationService {
    void save(Principal principal);
    void removeAllPrincipals();
    Principal findByName(String name);
}
