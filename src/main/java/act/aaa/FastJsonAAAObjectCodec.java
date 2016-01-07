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
import org.osgl.aaa.Privilege;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Serializer and Deserializer of {@link org.osgl.aaa.impl.SimplePermission} for FastJson
 */
public class FastJsonAAAObjectCodec implements ObjectSerializer, ObjectDeserializer {

    private Class<? extends AAAObject> type;
    private AAAPersistentService persistentService;

    public FastJsonAAAObjectCodec(Class<? extends AAAObject> type, AAAPersistentService persistentService) {
        this.type = type;
        this.persistentService = persistentService;
    }

    @Override
    public int getFastMatchToken() {
        return JSONToken.LITERAL_STRING;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        JSONLexer lexer = parser.getLexer();
        if (lexer.token() == JSONToken.LITERAL_STRING) {
            String text = lexer.stringVal();
            return (T) persistentService.findByName(text, this.type);
        }
        return null;
    }

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.getWriter();
        if (object == null) {
            out.writeNull();
            return;
        }
        out.write("\"" + ((AAAObject) object).getName() + "\"");
    }
}
