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

import bpsim.ElementParameters;
import org.eclipse.bpmn2.CatchEvent;
import org.eclipse.bpmn2.DataOutput;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.OutputSet;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tasks.Simulations;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;
import org.kie.workbench.common.stunner.bpmn.definition.property.simulation.SimulationAttributeSet;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class CatchEventPropertyWriter extends EventPropertyWriter {

    private final CatchEvent event;
    private ElementParameters simulationParameters;

    public CatchEventPropertyWriter(CatchEvent event) {
        super(event);
        this.event = event;
    }

    public void setAssignmentsInfo(AssignmentsInfo assignmentsInfo) {
        assignmentsInfo.getAssociations()
                .getOutputs()
                .stream()
                .map(declaration -> this.addDataOutputAssociation(declaration, assignmentsInfo.getOutputs()))
                .forEach(doa -> {
                    OutputSet outputSet = bpmn2.createOutputSet();
                    doa.getSourceRef().forEach(this::addBaseElement);
                    this.addBaseElement(doa.getTargetRef());
                    doa.getSourceRef().forEach(i -> {
                        DataOutput sourceRef = (DataOutput) i;
                        outputSet.getDataOutputRefs().add(sourceRef);
                    });
                    event.setOutputSet(outputSet);
                    event.getDataOutputAssociation().add(doa);
                });
    }

    public void setSimulationSet(SimulationAttributeSet simulationSet) {
        ElementParameters elementParameters = Simulations.toElementParameters(simulationSet);
        this.simulationParameters = elementParameters;
    }

    public ElementParameters getSimulationParameters() {
        return simulationParameters;
    }

    @Override
    public void addEventDefinition(EventDefinition eventDefinition) {
        this.event.getEventDefinitions().add(eventDefinition);
    }
}
