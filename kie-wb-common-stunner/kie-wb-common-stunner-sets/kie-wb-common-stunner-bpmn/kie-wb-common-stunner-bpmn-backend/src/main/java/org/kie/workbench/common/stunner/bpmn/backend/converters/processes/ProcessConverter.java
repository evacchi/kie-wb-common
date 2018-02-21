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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.LaneSet;
import org.eclipse.bpmn2.Process;
import org.kie.workbench.common.stunner.bpmn.backend.converters.FlowElementConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.GraphBuildingContext;
import org.kie.workbench.common.stunner.bpmn.backend.converters.LaneConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeResult;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.ProcessPropertyReader;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.PropertyReaderFactory;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagramImpl;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.AdHoc;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.DiagramSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.Executable;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.Id;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.Package;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.ProcessInstanceDescription;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.Version;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.Documentation;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.Name;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessData;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessVariables;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public class ProcessConverter {

    private final PropertyReaderFactory propertyReaderFactory;
    private final GraphBuildingContext context;
    private final FlowElementConverter flowElementConverter;
    private final LaneConverter laneConverter;
    private final TypedFactoryManager factoryManager;

    public ProcessConverter(
            TypedFactoryManager typedFactoryManager,
            PropertyReaderFactory propertyReaderFactory,
            GraphBuildingContext context) {

        this.factoryManager = typedFactoryManager;
        this.propertyReaderFactory = propertyReaderFactory;
        this.context = context;
        this.flowElementConverter = new FlowElementConverter(typedFactoryManager, propertyReaderFactory, context);
        this.laneConverter = new LaneConverter(typedFactoryManager, propertyReaderFactory);
    }

    public void convert(String definitionsId, Process process) {
        Node<View<BPMNDiagramImpl>, ?> firstDiagramNode =
                convertProcessNode(definitionsId, process);

        context.addNode(firstDiagramNode);

        Map<String, NodeResult<BPMNViewDefinition>> freeFloatingNodes =
                convertFlowElements(process.getFlowElements());

        convertLaneSets(process.getLaneSets(), freeFloatingNodes, firstDiagramNode);

        freeFloatingNodes.values()
                .forEach(n -> context.addChildNode(firstDiagramNode, n.value()));

        process.getFlowElements()
                .forEach(flowElementConverter::convertEdge);

        process.getFlowElements()
                .forEach(flowElementConverter::convertDockedNodes);
    }

    private void convertLaneSets(List<LaneSet> laneSets, Map<String, NodeResult<BPMNViewDefinition>> freeFloatingNodes, Node<View<BPMNDiagramImpl>, ?> firstDiagramNode) {
        laneSets
                .stream()
                .flatMap(laneSet -> laneSet.getLanes().stream())
                .forEach(lane -> {
                    Node<? extends View<? extends BPMNViewDefinition>, ?> laneNode =
                            laneConverter.convert(lane);
                    context.addChildNode(firstDiagramNode, laneNode);

                    lane.getFlowNodeRefs().forEach(node -> {
                        NodeResult<BPMNViewDefinition> child = freeFloatingNodes.remove(node.getId());
                        context.addChildNode(laneNode, child.value());
                    });
                });
    }

    private Map<String, NodeResult<BPMNViewDefinition>> convertFlowElements(List<FlowElement> flowElements) {
        return flowElements
                .stream()
                .map(flowElementConverter::convertNode)
                .filter(NodeResult::notIgnored)
                .collect(Collectors.toMap(NodeResult::getId, Function.identity()));
    }

    private Node<View<BPMNDiagramImpl>, ?> convertProcessNode(String id, Process process) {
        Node<View<BPMNDiagramImpl>, Edge> diagramNode =
                factoryManager.newNode(id, BPMNDiagramImpl.class);
        BPMNDiagramImpl definition = diagramNode.getContent().getDefinition();

        ProcessPropertyReader e = propertyReaderFactory.of(process);

        definition.setDiagramSet(new DiagramSet(
                new Name(process.getName()),
                new Documentation(e.getDocumentation()),
                new Id(process.getId()),
                new Package(e.getPackageName()),
                new Version(e.getVersion()),
                new AdHoc(e.isAdHoc()),
                new ProcessInstanceDescription(e.getDescription()),
                new Executable(process.isIsExecutable())
        ));

        definition.setProcessData(new ProcessData(
                new ProcessVariables(e.getProcessVariables())
        ));

        diagramNode.getContent().setBounds(e.getBounds());

        definition.setFontSet(e.getFontSet());
        definition.setBackgroundSet(e.getBackgroundSet());

        return diagramNode;
    }
}