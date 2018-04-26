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

package org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties;

import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.ThrowEvent;
import org.kie.workbench.common.stunner.bpmn.backend.converters.customproperties.ParsedAssignmentsInfo;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;

public class ThrowEventPropertyWriter extends EventPropertyWriter {

    private final ThrowEvent throwEvent;

    public ThrowEventPropertyWriter(ThrowEvent flowElement, VariableScope variableScope) {
        super(flowElement, variableScope);
        this.throwEvent = flowElement;
    }

    @Override
    public void setAssignmentsInfo(AssignmentsInfo info) {
        ParsedAssignmentsInfo assignmentsInfo = ParsedAssignmentsInfo.of(info);
        assignmentsInfo
                .getInputs().getDeclarations()
                .stream()
                .filter(varDecl -> varDecl.getType() != null)
                .map(varDecl -> new DeclarationWriter(flowElement.getId(), varDecl))
                .forEach(dw -> {
                    this.addItemDefinition(dw.getItemDefinition());
                    throwEvent.getDataInputs().add(dw.getDataInput());
                });

        assignmentsInfo.getAssociations()
                .getInputs()
                .stream()
                .map(declaration -> new InputAssignmentWriter(
                        flowElement.getId(),
                        // source is a variable
                        variableScope.lookup(declaration.getLeft()),
                        // target is an input
                        assignmentsInfo
                                .getInputs()
                                .lookup(declaration.getRight()))
                ).forEach(dia -> {
            throwEvent.getDataInputs().add(dia.getDataInput());
            throwEvent.setInputSet(dia.getInputSet());
        });
    }

    @Override
    protected void addEventDefinition(EventDefinition eventDefinition) {
        this.throwEvent.getEventDefinitions().add(eventDefinition);
    }
}
