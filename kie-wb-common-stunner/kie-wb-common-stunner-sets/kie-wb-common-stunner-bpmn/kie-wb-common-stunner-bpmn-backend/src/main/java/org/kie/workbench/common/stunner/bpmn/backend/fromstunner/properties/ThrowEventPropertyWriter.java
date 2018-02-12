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
import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.InputSet;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.Property;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssociationDeclaration;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class ThrowEventPropertyWriter extends EventPropertyWriter {

    private final EndEvent endEvent;

    public ThrowEventPropertyWriter(EndEvent flowElement) {
        super(flowElement);
        this.endEvent = flowElement;
    }

    public void setAssignmentsInfo(AssignmentsInfo assignmentsInfo) {
        assignmentsInfo.getAssociations()
                .forEach(this::addDataInputAssociation);
    }

    private void addDataInputAssociation(AssociationDeclaration a) {
        // first we declare the type of this assignment
        ItemDefinition typeDef =
                typedef(a.getSource(),
                        "java.lang.String");

        // then we declare a name (a variable) with that type,
        // e.g. foo:java.lang.String
        Property source = varDecl(a.getSource(), typeDef);

        // then we declare the input that will provide
        // the value that we assign to `source`
        // e.g. myInput
        DataInput target = readInputFrom(a.getTarget());

        // then we create the actual association between the two
        // e.g. foo := myInput (or, to put it differently, myInput -> foo)
        DataInputAssociation dataInputAssociation =
                associate(source, target);

        endEvent.getDataInputs().add(target);

        InputSet inputSet = bpmn2.createInputSet();
        inputSet.getDataInputRefs().add(target);
        endEvent.setInputSet(inputSet);

        this.addBaseElement(typeDef);

        endEvent.getDataInputAssociation()
                .add(dataInputAssociation);
    }

    private DataInputAssociation associate(Property source, DataInput dataInput) {
        DataInputAssociation dataInputAssociation =
                bpmn2.createDataInputAssociation();

        dataInputAssociation
                .getSourceRef()
                .add(source);

        dataInputAssociation
                .setTargetRef(dataInput);
        return dataInputAssociation;
    }

    private DataInput readInputFrom(String targetName) {
        DataInput dataInput = bpmn2.createDataInput();
        dataInput.setName(targetName);
        // the id is an encoding of the node id + the name of the input
        dataInput.setId(makeDataInputId(targetName));
        return dataInput;
    }

    private String makeDataInputId(String targetName) {
        return endEvent.getId() + "_" + targetName + "InputX";
    }

    private Property varDecl(String varName, ItemDefinition typeDef) {
        Property source = bpmn2.createProperty();
        source.setId(varName);
        source.setItemSubjectRef(typeDef);
        return source;
    }

    private ItemDefinition typedef(String sourceName, String type) {
        ItemDefinition typeDef = bpmn2.createItemDefinition();
        typeDef.setId("_" + sourceName + "Item");
        typeDef.setStructureRef(type);
        return typeDef;
    }

    @Override
    protected void addEventDefinition(EventDefinition eventDefinition) {
        this.endEvent.getEventDefinitions().add(eventDefinition);
    }
}
