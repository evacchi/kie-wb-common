/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.bpmn.backend.converters.properties;

import org.eclipse.bpmn2.BusinessRuleTask;

public class BusinessRuleTaskPropertyReader extends AbstractPropertyReader {

    private final BusinessRuleTask task;

    public BusinessRuleTaskPropertyReader(BusinessRuleTask task) {
        super(task);
        this.task = task;
    }

    public String getRuleFlowGroup() {
        return attribute("ruleFlowGroup");
    }

    public String getAssignmentsInfo() {
        return Properties.getAssignmentsInfo(task);
    }

    public boolean isAsync() {
        return Boolean.parseBoolean(metaData("customAsync"));
    }

    public boolean isAdHocAutoStart() {
        return Boolean.parseBoolean(metaData("customAutoStart"));
    }

    public String getOnEntryAction() {
        return Scripts.onEntry(element.getExtensionValues());
    }

    public String getOnExitAction() {
        return Scripts.onExit(element.getExtensionValues());
    }

    public String getScriptLanguage() {
        return Scripts.scriptLanguage(element.getExtensionValues());
    }
}