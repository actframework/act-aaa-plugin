package act.aaa;

import org.osgl.aaa.AAAPersistentService;
import org.osgl.aaa.Role;

import javax.inject.Inject;

/**
 * Serializer and Deserializer of {@link Role} for FastJson
 */
public class FastJsonRoleCodec extends FastJsonAAAObjectCodec {

    @Inject
    public FastJsonRoleCodec(AAAPersistentService persistentService) {
        super(Role.class, persistentService);
    }

}
