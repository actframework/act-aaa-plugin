package act.aaa;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import org.osgl.aaa.AAAObject;
import org.osgl.aaa.AAAPersistentService;
import org.osgl.aaa.Permission;
import org.osgl.aaa.Privilege;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Serializer and Deserializer of {@link org.osgl.aaa.Privilege} for FastJson
 */
public class FastJsonPrivilegeCodec extends FastJsonAAAObjectCodec {

    @Inject
    public FastJsonPrivilegeCodec(AAAPersistentService persistentService) {
        super(Privilege.class, persistentService);
    }

}
