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

import act.aaa.model.AuditBase;
import act.app.App;
import act.app.event.SysEventId;
import act.conf.AppConfig;
import act.job.OnSysEvent;
import act.util.AnnotatedClassFinder;
import act.util.SubClassFinder;
import org.osgl.aaa.Principal;
import org.osgl.exception.ConfigurationException;
import org.osgl.util.E;
import org.osgl.util.S;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class AAAMetaInfo {

    Class<?> principalEntityType;
    Method passwordVerifier;
    Method roleProvider;
    Method permissionProvider;
    Method privilegeProvider;

    Set<Class> userTypes = new HashSet<>();
    Set<Class> auditTypes = new HashSet<>();

    @Inject
    private AppConfig config;

    @Inject
    private App app;

    @AnnotatedClassFinder(PrincipalEntity.class)
    public void foundUserType(Class type) {
        if (config.possibleControllerClass(type.getName())) {
            userTypes.add(type);
        }
    }

    @SubClassFinder
    public void foundUserType2(Class<Principal> type) {
        String typeName = type.getName();
        if (config.possibleControllerClass(typeName) && !typeName.startsWith("act.")) {
            userTypes.add(type);
        }
    }

    @SubClassFinder
    public void foundAuditType(Class<AuditBase> type) {
        String typeName = type.getName();
        if (config.possibleControllerClass(typeName) && !typeName.startsWith("act.")) {
            auditTypes.add(type);
        }
    }

    @OnSysEvent(SysEventId.PRE_START)
    public void explore() {
        principalEntityType = principalEntityType();
        if (null == principalEntityType) {
            return;
        }
        Method[] methods = principalEntityType.getMethods();
        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            if (method.isAnnotationPresent(PasswordVerifier.class)) {
                passwordVerifierFound(method);
            } else if (method.isAnnotationPresent(RoleProvider.class)) {
                roleProviderFound(method);
            } else if (method.isAnnotationPresent(PermissionProvider.class)) {
                permissionProviderFound(method);
            } else if (method.isAnnotationPresent(PrivilegeProvider.class)) {
                privilegeProviderFound(method);
            }
        }
    }

    private void passwordVerifierFound(Method method) {
        E.invalidConfigurationIf(passwordVerifier != null, "Multiple password verifier method found on: " + principalEntityType);
        Class<?> retType = method.getReturnType();
        E.invalidConfigurationIf((boolean.class != retType && Boolean.class != retType), "Password verifier method return type shall be boolean: " + method);
        Class<?>[] paramTypes = method.getParameterTypes();
        E.invalidConfigurationIf(paramTypes.length != 1, "Single parameter excepted on password verifier method: " + method);
        Class<?> paramType = paramTypes[0];
        E.invalidConfigurationIf(char[].class != paramType && String.class != paramType, "String or char[] parameter expected on password verifier method: " + method);
        passwordVerifier = method;
    }

    private void roleProviderFound(Method method) {
        validateProviderMethod(method, roleProvider,
                String.class, null, null, "role provider");
        roleProvider = method;
    }

    private void permissionProviderFound(Method method) {
        validateProviderMethod(method, permissionProvider,
                String.class, null, null, "permission provider");
        permissionProvider = method;
    }

    private void privilegeProviderFound(Method method) {
        validateProviderMethod(method, permissionProvider, String.class, int.class, Integer.class, "privilege provider");
        privilegeProvider = method;
    }

    private void validateProviderMethod(
            Method found,
            Method existed,
            Class<?> retTypeExpected,
            Class<?> retTypeExpected2,
            Class<?> retTypeExpected3,
            String name
    ) {
        E.invalidConfigurationIf(null != existed, "Multiple " + name + " method found on: " + principalEntityType);
        Class<?> retType = found.getReturnType();
        E.invalidConfigurationIf(
                retTypeExpected != retType && retTypeExpected2 != retType && retTypeExpected3 != retType,
                name + " method return type shall be String: " + found);
        Class<?>[] paramTypes = found.getParameterTypes();
        E.invalidConfigurationIf(0 != paramTypes.length, name + " method shall not have parameters: " + found);
    }

    private Class<?> principalEntityType() {
        String s = AAAConfig.PRINCIPAL_MODEL.get();
        if (S.notBlank(s)) {
            try {
                return app.classForName(s);
            } catch (Exception e) {
                throw new ConfigurationException("Invalid principal model: " + s);
            }
        } else {
            if (userTypes.size() == 1) {
                return userTypes.iterator().next();
            }
        }
        return null;
    }
}
