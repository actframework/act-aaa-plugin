package act.aaa.model;

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

import act.db.Model;
import act.util.SimpleBean;
import org.osgl.aaa.Auditor;
import org.osgl.aaa.Principal;
import org.osgl.util.S;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AuditBase implements SimpleBean {

    public String message;
    public String target;
    public String principal;
    public String privilege;
    public boolean success;
    public String permission;

    public AuditBase() {}

    public AuditBase(Object aTarget, Principal aPrincipal, String aPermission, String aPrivilege, boolean theSuccess, String aMessage) {
        this.message = aMessage;
        this.target = targetStr(aTarget);
        this.principal = aPrincipal.getName();
        this.permission = aPermission;
        this.privilege = aPrivilege;
        this.success = theSuccess;
    }

    private String targetStr(Object target) {
        if (target instanceof Auditor.Target) {
            return ((Auditor.Target) target).auditTag();
        } else if (target instanceof Model) {
            return S.concat(target.getClass().getSimpleName(), "[", S.string(((Model) target)._id()), "]");
        } else {
            return S.string(target);
        }
    }

}
