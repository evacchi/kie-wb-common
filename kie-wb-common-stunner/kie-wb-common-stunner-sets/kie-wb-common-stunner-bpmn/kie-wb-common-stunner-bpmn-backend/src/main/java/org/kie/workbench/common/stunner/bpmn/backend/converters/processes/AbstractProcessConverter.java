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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.LaneSet;
import org.kie.workbench.common.stunner.bpmn.backend.converters.FlowElementConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.GraphBuildingContext;
import org.kie.workbench.common.stunner.bpmn.backend.converters.LaneConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeResult;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.PropertyReaderFactory;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.bpmn.definition.Lane;

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

    protected void convertNodes(NodeResult firstNode, List<FlowElement> flowElements, List<LaneSet> laneSets) {
        Map<String, NodeResult> freeFloatingNodes =
                convertFlowElements(flowElements);

        freeFloatingNodes.values()
                .forEach(n -> n.setParent(firstNode));

        convertLaneSets(laneSets, freeFloatingNodes, firstNode);

        updatePositions(firstNode);

        flowElements
                .forEach(flowElementConverter::convertEdge);

        flowElements
                .forEach(flowElementConverter::convertDockedNodes);
    }

    private void updatePositions(NodeResult firstNode) {
        context.addNode(firstNode.value());
        Deque<NodeResult.Success> workingSet = new ArrayDeque<>(firstNode.getChildren());
        while (!workingSet.isEmpty()) {
            NodeResult.Success current = workingSet.pop();
            context.addChildNode(current.getParent().value(), current.value());
            workingSet.addAll(current.getChildren());
        }
    }

    private void convertLaneSets(List<LaneSet> laneSets, Map<String, NodeResult> freeFloatingNodes, NodeResult firstDiagramNode) {
        laneSets.stream()
                .flatMap(laneSet -> laneSet.getLanes().stream())
                .forEach(lane -> {
                    NodeResult laneNode = laneConverter.convert(lane);
                    laneNode.setParent(firstDiagramNode);

                    lane.getFlowNodeRefs().forEach(node -> {
                        freeFloatingNodes.get(node.getId()).setParent(laneNode);
                    });
                });
    }

    private Map<String, NodeResult> convertFlowElements(List<FlowElement> flowElements) {
        LinkedHashMap<String, NodeResult> result = new LinkedHashMap<>();

        flowElements
                .stream()
                .map(flowElementConverter::convertNode)
                .filter(NodeResult::notIgnored)
                .forEach(n -> result.put(n.getId(), n));

        return result;
    }
}