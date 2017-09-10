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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import act.app.ActionContext;
import act.app.App;
import act.conf.AppConfig;
import act.event.EventBus;
import act.job.AppJobManager;
import act.route.Router;
import act.util.ClassNames;
import org.junit.Ignore;
import org.mockito.internal.matchers.StartsWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgl.$;
import org.osgl.http.H;
import org.osgl.ut.TestBase;
import org.osgl.util.IO;

import java.io.InputStream;
import java.lang.reflect.Field;

@Ignore
public class AAATestBase extends TestBase {


    protected Router mockRouter;
    protected ActionContext mockActionContext;
    protected AppConfig mockAppConfig;
    protected AppJobManager mockJobManager;
    protected App mockApp;
    protected H.Request mockReq;
    protected H.Response mockResp;
    protected EventBus mockEventBus;

    protected void setup() throws Exception {
        mockApp = mock(App.class);
        Field f = App.class.getDeclaredField("INST");
        f.setAccessible(true);
        f.set(null, mockApp);
        mockJobManager = mock(AppJobManager.class);
        mockEventBus = mock(EventBus.class);
        when(mockApp.jobManager()).thenReturn(mockJobManager);
        when(mockApp.eventBus()).thenReturn(mockEventBus);
        mockAppConfig = mock(AppConfig.class);
        when(mockAppConfig.possibleControllerClass(argThat(new StartsWith("testapp.controller.")))).thenReturn(true);
        mockActionContext = mock(ActionContext.class);
        when(mockActionContext.app()).thenReturn(mockApp);
        when(mockActionContext.config()).thenReturn(mockAppConfig);
        mockRouter = mock(Router.class);
        when(mockApp.config()).thenReturn(mockAppConfig);
        when(mockApp.router()).thenReturn(mockRouter);
        when(mockApp.getInstance(any(Class.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return $.newInstance((Class)args[0]);
            }
        });
        mockReq = mock(H.Request.class);
        mockResp = mock(H.Response.class);
    }

    protected byte[] loadBytecode(String className) {
        String fileName = ClassNames.classNameToClassFileName(className);
        InputStream is = getClass().getResourceAsStream(fileName);
        return IO.readContent(is);
    }

}
