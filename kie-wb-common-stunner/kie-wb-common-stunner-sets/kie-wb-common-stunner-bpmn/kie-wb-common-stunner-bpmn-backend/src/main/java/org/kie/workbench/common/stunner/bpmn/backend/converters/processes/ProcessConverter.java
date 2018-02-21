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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.LaneSet;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.SubProcess;
import org.kie.workbench.common.stunner.bpmn.backend.converters.BpmnEdge;
import org.kie.workbench.common.stunner.bpmn.backend.converters.BpmnNode;
import org.kie.workbench.common.stunner.bpmn.backend.converters.FlowElementConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.LaneConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.ProcessPropertyReader;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.PropertyReaderFactory;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.SubProcessPropertyReader;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagramImpl;
import org.kie.workbench.common.stunner.bpmn.definition.EmbeddedSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.EventSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.AdHoc;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.DiagramSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.Executable;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.Id;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.Package;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.ProcessInstanceDescription;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.Version;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.Documentation;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.Name;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessData;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessVariables;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public class ProcessConverter {

    protected final PropertyReaderFactory propertyReaderFactory;
    protected final FlowElementConverter flowElementConverter;
    protected final LaneConverter laneConverter;
    protected final TypedFactoryManager factoryManager;

    public ProcessConverter(
            TypedFactoryManager typedFactoryManager,
            PropertyReaderFactory propertyReaderFactory) {

        this.factoryManager = typedFactoryManager;
        this.propertyReaderFactory = propertyReaderFactory;
        this.flowElementConverter = new FlowElementConverter(factoryManager, propertyReaderFactory, this);
        this.laneConverter = new LaneConverter(typedFactoryManager, propertyReaderFactory);
    }

    public BpmnNode convertProcess(String id, Process process) {
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

        BpmnNode firstNode = BpmnNode.of(diagramNode);

        List<FlowElement> flowElements = process.getFlowElements();
        List<LaneSet> laneSets = process.getLaneSets();

        Map<String, BpmnNode> nodes = convertNodes(firstNode, flowElements, laneSets);
        List<BpmnEdge> bpmnEdges = convertEdges(flowElements, nodes);
        firstNode.addAllEdges(bpmnEdges);

        return firstNode;
    }

    public BpmnNode convertSubProcess(SubProcess subProcess) {
        BpmnNode result =
                subProcess.isTriggeredByEvent() ?
                        convertEventSubprocess(subProcess)
                        : convertEmbeddedSubprocess(subProcess);

        List<FlowElement> flowElements = subProcess.getFlowElements();
        List<LaneSet> laneSets = subProcess.getLaneSets();
        Map<String, BpmnNode> nodes = convertNodes(result, flowElements, laneSets);
        List<BpmnEdge> bpmnEdges = convertEdges(flowElements, nodes);
        result.addAllEdges(bpmnEdges);
        return result;
    }

    private BpmnNode convertSubProcessNode(SubProcess subProcess) {
        if (subProcess.isTriggeredByEvent()) {
            return convertEventSubprocess(subProcess);
        } else {
            return convertEmbeddedSubprocess(subProcess);
        }
    }

    private BpmnNode convertEmbeddedSubprocess(SubProcess subProcess) {
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

    private BpmnNode convertEventSubprocess(SubProcess subProcess) {
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
    }

    protected Map<String, BpmnNode> convertNodes(BpmnNode firstNode, List<FlowElement> flowElements, List<LaneSet> laneSets) {
        Map<String, BpmnNode> freeFloatingNodes =
                convertFlowElements(flowElements);

        freeFloatingNodes.values()
                .forEach(n -> n.setParent(firstNode));

        convertLaneSets(laneSets, freeFloatingNodes, firstNode);

        return freeFloatingNodes;
    }

    protected List<BpmnEdge> convertEdges(List<FlowElement> flowElements, Map<String, BpmnNode> nodes) {
        return flowElements.stream()
                .map(e -> flowElementConverter.convertEdge(e, nodes))
                .filter(Result::isSuccess)
                .map(Result::value)
                .collect(Collectors.toList());
        }

    private void convertLaneSets(List<LaneSet> laneSets, Map<String, BpmnNode> freeFloatingNodes, BpmnNode firstDiagramNode) {
        laneSets.stream()
                .flatMap(laneSet -> laneSet.getLanes().stream())
                .forEach(lane -> {
                    BpmnNode laneNode = laneConverter.convert(lane);
                    laneNode.setParent(firstDiagramNode);

                    lane.getFlowNodeRefs().forEach(node -> {
                        freeFloatingNodes.get(node.getId()).setParent(laneNode);
                    });
                });
    }

    private Map<String, BpmnNode> convertFlowElements(List<FlowElement> flowElements) {
        LinkedHashMap<String, BpmnNode> result = new LinkedHashMap<>();

        flowElements
                .stream()
                .map(flowElementConverter::convertNode)
                .filter(Result::isSuccess)
                .map(Result::value)
                .forEach(n -> result.put(n.value().getUUID(), n));

        return result;
    }
}