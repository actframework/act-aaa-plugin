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

import com.alibaba.fastjson.JSON;
import org.osgl.aaa.AAAPersistentService;
import org.osgl.aaa.Privilege;
import org.osgl.util.C;
import org.osgl.util.S;

import javax.inject.Inject;
import java.util.List;

/**
 * Serializer and Deserializer of {@link org.osgl.aaa.Privilege} for FastJson
 */
public class FastJsonPrivilegeCodec extends FastJsonAAAObjectCodec {

    @Inject
    public FastJsonPrivilegeCodec(AAAPersistentService persistentService) {
        super(Privilege.class, persistentService);
    }


    public static void main() {
        List<Object> list = C.list();
        String json = S.strip(JSON.toJSONString(list)).of("\"");
    }
}
