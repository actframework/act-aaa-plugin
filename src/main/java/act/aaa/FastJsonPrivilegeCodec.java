package act.aaa;

import org.osgl.aaa.AAAPersistentService;
import org.osgl.aaa.Privilege;

import javax.inject.Inject;

/**
 * Serializer and Deserializer of {@link org.osgl.aaa.Privilege} for FastJson
 */
public class FastJsonPrivilegeCodec extends FastJsonAAAObjectCodec {

    @Inject
    public FastJsonPrivilegeCodec(AAAPersistentService persistentService) {
        super(Privilege.class, persistentService);
    }

}
