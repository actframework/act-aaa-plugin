package act.aaa;

/*-
 * #%L
 * ACT AAA Plugin
 * %%
 * Copyright (C) 2015 - 2017 ActFramework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import org.osgl.aaa.AAAObject;
import org.osgl.aaa.AAAPersistentService;

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
            lexer.nextToken(JSONToken.COMMA);
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
