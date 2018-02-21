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

package org.kie.workbench.common.stunner.bpmn.backend.converters.processes;

import java.util.List;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.LaneSet;
import org.eclipse.bpmn2.SubProcess;
import org.kie.workbench.common.stunner.bpmn.backend.converters.FlowElementConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.GraphBuildingContext;
import org.kie.workbench.common.stunner.bpmn.backend.converters.BpmnNode;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.PropertyReaderFactory;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.SubProcessPropertyReader;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.bpmn.definition.EmbeddedSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.EventSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.Documentation;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.Name;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessData;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessVariables;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public class SubProcessConverter extends AbstractProcessConverter {

    public SubProcessConverter(TypedFactoryManager typedFactoryManager, PropertyReaderFactory propertyReaderFactory, FlowElementConverter flowElementConverter, GraphBuildingContext context) {
        super(typedFactoryManager, propertyReaderFactory, flowElementConverter, context);
    }

    public BpmnNode convert(SubProcess subProcess) {
        BpmnNode result = convertSubProcessNode(subProcess);
        List<FlowElement> flowElements = subProcess.getFlowElements();
        List<LaneSet> laneSets = subProcess.getLaneSets();
        convertNodes(result, flowElements, laneSets);
        return result;
    }

    private BpmnNode convertSubProcessNode(SubProcess subProcess) {
        if (subProcess.isTriggeredByEvent()) {
            Node<View<EventSubprocess>, Edge> node = factoryManager.newNode(subProcess.getId(), EventSubprocess.class);

            EventSubprocess definition = node.getContent().getDefinition();
            SubProcessPropertyReader p = propertyReaderFactory.of(subProcess);

            definition.setGeneral(new BPMNGeneralSet(
                    new Name(subProcess.getName()),
                    new Documentation(p.getDocumentation())
            ));

            definition.getIsAsync().setValue(p.isAsync());

            definition.setProcessData(new ProcessData(
                    new ProcessVariables(p.getProcessVariables())));

            definition.setSimulationSet(p.getSimulationSet());

            definition.setDimensionsSet(p.getRectangleDimensionsSet());
            definition.setFontSet(p.getFontSet());
            definition.setBackgroundSet(p.getBackgroundSet());

            node.getContent().setBounds(p.getBounds());

            return BpmnNode.of(node);
        } else {
            Node<View<EmbeddedSubprocess>, Edge> node = factoryManager.newNode(subProcess.getId(), EmbeddedSubprocess.class);

            EmbeddedSubprocess definition = node.getContent().getDefinition();
            SubProcessPropertyReader p = propertyReaderFactory.of(subProcess);

            definition.setGeneral(new BPMNGeneralSet(
                    new Name(subProcess.getName()),
                    new Documentation(p.getDocumentation())
            ));

            definition.getOnEntryAction().setValue(p.getOnEntryAction());
            definition.getOnExitAction().setValue(p.getOnExitAction());
            definition.getIsAsync().setValue(p.isAsync());

            definition.setProcessData(new ProcessData(
                    new ProcessVariables(p.getProcessVariables())));

            definition.setSimulationSet(p.getSimulationSet());

            definition.setDimensionsSet(p.getRectangleDimensionsSet());
            definition.setFontSet(p.getFontSet());
            definition.setBackgroundSet(p.getBackgroundSet());

            node.getContent().setBounds(p.getBounds());
            return BpmnNode.of(node);
        }
    }
}
