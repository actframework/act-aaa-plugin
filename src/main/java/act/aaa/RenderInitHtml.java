package act.aaa;

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

import act.inject.util.LoadResource;
import org.osgl.mvc.result.RenderHtml;

import javax.inject.Singleton;

@Singleton
public class RenderInitHtml extends RenderHtml {

    @LoadResource("act/aaa/init.html")
    private String initHtml;

    public RenderInitHtml() {
        super("");
    }

    @Override
    public String content() {
        return initHtml;
    }
}
