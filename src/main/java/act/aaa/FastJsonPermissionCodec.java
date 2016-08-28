package act.aaa;

import org.osgl.aaa.AAAPersistentService;
import org.osgl.aaa.Permission;

import javax.inject.Inject;

/**
 * Serializer and Deserializer of {@link org.osgl.aaa.Permission} for FastJson
 */
public class FastJsonPermissionCodec extends FastJsonAAAObjectCodec {

    @Inject
    public FastJsonPermissionCodec(AAAPersistentService persistentService) {
        super(Permission.class, persistentService);
    }

}
