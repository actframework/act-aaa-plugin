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
