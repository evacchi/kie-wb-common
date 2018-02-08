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

package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.InputSet;
import org.eclipse.bpmn2.ThrowEvent;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class ThrowEventPropertyWriter extends EventPropertyWriter {

    private final ThrowEvent throwEvent;

    public ThrowEventPropertyWriter(ThrowEvent flowElement) {
        super(flowElement);
        this.throwEvent = flowElement;
    }

    @Override
    public void setAssignmentsInfo(AssignmentsInfo assignmentsInfo) {
        assignmentsInfo.getAssociations().getInputs()
                .stream()
                .map(declaration -> addDataInputAssociation(declaration, assignmentsInfo.getInputs()))
                .forEach(dia -> {
                    InputSet inputSet = bpmn2.createInputSet();
                    inputSet.getDataInputRefs().add((DataInput) dia.getTargetRef());
                    throwEvent.setInputSet(inputSet);
                    dia.getSourceRef().forEach(this::addBaseElement);
                    throwEvent.getDataInputAssociation().add(dia);
                });
    }

    @Override
    protected void addEventDefinition(EventDefinition eventDefinition) {
        this.throwEvent.getEventDefinitions().add(eventDefinition);
    }
}
