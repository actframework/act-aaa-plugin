package act.aaa.util;

/*-
 * #%L
 * ACT AAA Plugin
 * %%
 * Copyright (C) 2015 - 2018 ActFramework
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

import org.osgl.aaa.AAA;
import org.osgl.aaa.Privilege;
import org.osgl.aaa.impl.SimplePrivilege;

public final class PrivilegeCache {

    public static final int MAX_PRIVILEGE_LEVEL = 99;
    public static final int MIN_PRIVILEGE_LEVEL = 0;

    public static final Privilege SUPERUSER = new SimplePrivilege("SU", AAA.SUPER_USER);
    public static final Privilege USER = new SimplePrivilege("USER", 0);

    private static Privilege[] cache = new Privilege[MAX_PRIVILEGE_LEVEL + 1];

    static {
        for (int i = MIN_PRIVILEGE_LEVEL; i <= MAX_PRIVILEGE_LEVEL; ++i) {
            cache[i] = new SimplePrivilege("P" + i, i);
        }
    }

    public static Privilege get(int i) {
        if (i <= MIN_PRIVILEGE_LEVEL) {
            return USER;
        } else if (i > MAX_PRIVILEGE_LEVEL) {
            return SUPERUSER;
        }
        return cache[i];
    }

}
