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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.LaneSet;
import org.kie.workbench.common.stunner.bpmn.backend.converters.BpmnEdge;
import org.kie.workbench.common.stunner.bpmn.backend.converters.BpmnNode;
import org.kie.workbench.common.stunner.bpmn.backend.converters.FlowElementConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.GraphBuildingContext;
import org.kie.workbench.common.stunner.bpmn.backend.converters.LaneConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.PropertyReaderFactory;

public abstract class AbstractProcessConverter {

    protected final PropertyReaderFactory propertyReaderFactory;
    protected final GraphBuildingContext context;
    protected final FlowElementConverter flowElementConverter;
    protected final LaneConverter laneConverter;
    protected final TypedFactoryManager factoryManager;

    public AbstractProcessConverter(
            TypedFactoryManager typedFactoryManager,
            PropertyReaderFactory propertyReaderFactory,
            FlowElementConverter flowElementConverter,
            GraphBuildingContext context) {

        this.factoryManager = typedFactoryManager;
        this.propertyReaderFactory = propertyReaderFactory;
        this.context = context;
        this.flowElementConverter = flowElementConverter;
        this.laneConverter = new LaneConverter(typedFactoryManager, propertyReaderFactory);
    }

    public AbstractProcessConverter(TypedFactoryManager typedFactoryManager, PropertyReaderFactory propertyReaderFactory, GraphBuildingContext context) {
        this(typedFactoryManager, propertyReaderFactory, new FlowElementConverter(typedFactoryManager, propertyReaderFactory, context), context);
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
        List<BpmnEdge> collect = flowElements.stream()
                .map(e -> flowElementConverter.convertEdge(e, nodes))
                .filter(Result::isSuccess)
                .map(Result::value)
                .collect(Collectors.toList());

//        flowElements
//                .forEach(flowElementConverter::convertDockedNodes);
        return collect;
    }

    protected void createEdges(BpmnNode firstNode) {
        context.addNode(firstNode.value());
        firstNode.getEdges().forEach(context::addEdge);
        Deque<BpmnNode> workingSet = new ArrayDeque<>(firstNode.getChildren());
        Set<BpmnNode> workedOff = new HashSet<>();
        while (!workingSet.isEmpty()) {
            BpmnNode current = workingSet.pop();
            if (workedOff.contains(current)) continue;
            workedOff.add(current);
            workingSet.addAll(current.getChildren());
            System.out.println(current.getParent().value().getUUID()+" :: " +current.value().getUUID());
            context.addChildNode(current.getParent().value(), current.value());
            current.getEdges().forEach(context::addEdge);
        }
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