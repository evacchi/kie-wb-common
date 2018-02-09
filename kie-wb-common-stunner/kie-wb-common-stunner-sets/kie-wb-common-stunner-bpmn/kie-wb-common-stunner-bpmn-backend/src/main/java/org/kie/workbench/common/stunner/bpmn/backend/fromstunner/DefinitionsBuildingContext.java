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

package org.kie.workbench.common.stunner.bpmn.backend.fromstunner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagramImpl;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.definition.Definition;
import org.kie.workbench.common.stunner.core.graph.content.definition.DefinitionSet;
import org.kie.workbench.common.stunner.core.graph.content.relationship.Child;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;
import org.kie.workbench.common.stunner.core.graph.util.GraphUtils;

//
// the implementation for this class is in the package-private
// abstract class below. We are using type parameters to fake type aliases.
// Scroll down for details
//
public class DefinitionsBuildingContext
        extends DefinitionsContextHelper<
        /*EdgeT = */
        Edge<ViewConnector<BPMNViewDefinition>,
                Node<? extends View<? extends BPMNViewDefinition>, ?>>,

        /*NodeT = */
        Node<View<? extends BPMNViewDefinition>,
                Edge<ViewConnector<BPMNViewDefinition>,
                        Node<? extends View<? extends BPMNViewDefinition>, ?>>>
        > {

    // constructor uses raw Graph for convenience

    public DefinitionsBuildingContext(
            Graph<DefinitionSet,
                    Node<View<? extends BPMNViewDefinition>,
                            Edge<ViewConnector<BPMNViewDefinition>,
                                    Node<? extends View<? extends BPMNViewDefinition>, ?>>>> graph) {
        super(graph);
    }
}

//
// this is sort-of a hack: we don't have type aliases in Java
// so we use this abstract class to bind a type-parameter to this horribly long
// Node, Edge declarations (because Node, Edge are... mutually recursive... erm)
// so we declare EdgeT, NodeT to "extend" the type we want to alias
// then in the concrete instance we actually **bind** them to the exact type
//
abstract class DefinitionsContextHelper<
        EdgeT extends
                Edge<ViewConnector<BPMNViewDefinition>,
                        Node<? extends View<? extends BPMNViewDefinition>, ?>>,
        NodeT extends
                Node<View<? extends BPMNViewDefinition>, EdgeT>
        > {

    private final Map<String, NodeT> nodes;

    private final Map<String, FlowNode> flowNodes;

    private final Node<Definition<BPMNDiagramImpl>, ?> firstNode;
    private final Graph<DefinitionSet, NodeT> graph;
    private Map<String, org.eclipse.bpmn2.SequenceFlow> sequenceFlows;
    private Map<String, BaseElement> baseElements;

    public DefinitionsContextHelper(Graph<DefinitionSet, NodeT> graph) {
        this.graph = graph;
        this.firstNode =
                GraphUtils.getFirstNode((Graph) graph, BPMNDiagramImpl.class);

        this.nodes =
                StreamSupport
                        .stream(graph.nodes().spliterator(), false)
                        .filter(n -> !firstNode.getUUID().equals(n.getUUID()))
                        .collect(Collectors.toMap(Node::getUUID, Function.identity()));

        this.flowNodes = new HashMap<>();
        this.sequenceFlows = new HashMap<>();
        this.baseElements = new HashMap<>();
    }

    public Stream<NodeT> nodes() {
        return nodes.values().stream();
    }

    public NodeT getNode(String id) {
        return nodes.get(id);
    }

    public Node<Definition<BPMNDiagramImpl>, ?> firstNode() {
        return firstNode;
    }

    public void addFlowNode(FlowNode flowNode) {
        flowNodes.put(flowNode.getId(), flowNode);
    }

    public FlowNode getFlowNode(String id) {
        return flowNodes.get(id);
    }

    public Collection<FlowNode> getFlowNodes() {
        return flowNodes.values();
    }

    public void addSequenceFlow(org.eclipse.bpmn2.SequenceFlow seq) {
        sequenceFlows.put(seq.getId(), seq);
    }

    public org.eclipse.bpmn2.SequenceFlow getSequenceFlow(String id) {
        return sequenceFlows.get(id);
    }

    public Collection<org.eclipse.bpmn2.SequenceFlow> getSequenceFlows() {
        return sequenceFlows.values();
    }

    public void addBaseElement(BaseElement element) {
        baseElements.put(element.getId(), element);
    }

    public BaseElement getBaseElements(String id) {
        return baseElements.get(id);
    }

    public Collection<BaseElement> getBaseElements() {
        return baseElements.values();
    }

    public Stream<EdgeT> edges() {
        return nodes()
                .flatMap(e -> Stream.concat(
                        e.getInEdges().stream(),
                        e.getOutEdges().stream()))
                .distinct()
                .filter(e -> !(e.getContent() instanceof Child));
    }

    public Graph<DefinitionSet, NodeT> getGraph() {
        return graph;
    }
}
