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

import java.util.Collections;

import org.eclipse.bpmn2.BusinessRuleTask;
import org.eclipse.bpmn2.InputOutputSpecification;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.kie.workbench.common.stunner.bpmn.backend.converters.DefinitionResolver;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;

public class BusinessRuleTaskPropertyReader extends TaskPropertyReader {

    public BusinessRuleTaskPropertyReader(BusinessRuleTask task, BPMNPlane plane, DefinitionResolver definitionResolver) {
        super(task, plane, definitionResolver);
    }

    public String getRuleFlowGroup() {
        return attribute("ruleFlowGroup");
    }

    public AssignmentsInfo getAssignmentsInfo() {
        InputOutputSpecification ioSpecification = task.getIoSpecification();
        if (ioSpecification == null) {
            return AssignmentsInfos.of(
                    Collections.emptyList(),
//                            Collections.emptyList(),
                    task.getDataInputAssociations(),
                    Collections.emptyList(),
//                            Collections.emptyList(),
                    task.getDataOutputAssociations(),
                    false);
        } else {
            return AssignmentsInfos.of(
                    ioSpecification.getDataInputs(),
                    //ioSpecification.getInputSets(),
                    task.getDataInputAssociations(),
                    ioSpecification.getDataOutputs(),
                    //ioSpecification.getOutputSets(),
                    task.getDataOutputAssociations(),
                    true);
        }
    }

    public boolean isAsync() {
        return Boolean.parseBoolean(metaData("customAsync"));
    }

    public boolean isAdHocAutoStart() {
        return Boolean.parseBoolean(metaData("customAutoStart"));
    }
}
