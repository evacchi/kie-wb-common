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

package org.kie.workbench.common.stunner.bpmn.backend.unconverters;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.bpmn2.FlowNode;
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

public class UnconverterContext {

    public final Map<String,
            Node<View<? extends BPMNViewDefinition>,
                    Edge<ViewConnector<BPMNViewDefinition>,
                            Node<? extends View<? extends BPMNViewDefinition>, ?>>>> nodes;

    public final Map<String, FlowNode> flowNodes;

    private final Node<Definition<BPMNDiagramImpl>, ?> firstNode;

    public UnconverterContext(
            Graph<DefinitionSet,
                    Node<View<? extends BPMNViewDefinition>,
                            Edge<ViewConnector<BPMNViewDefinition>,
                                    Node<? extends View<? extends BPMNViewDefinition>, ?>>>> graph) {
        this.firstNode =
                GraphUtils.getFirstNode((Graph) graph, BPMNDiagramImpl.class);

        this.nodes =
                StreamSupport
                        .stream(graph.nodes().spliterator(), false)
                        .filter(n -> !firstNode.getUUID().equals(n.getUUID()))
                        .collect(Collectors.toMap(Node::getUUID, Function.identity()));

        this.flowNodes = new HashMap<>();
    }

    public Stream<
            Node<View<? extends BPMNViewDefinition>,
                    Edge<ViewConnector<BPMNViewDefinition>,
                            Node<? extends View<? extends BPMNViewDefinition>, ?>>>> nodes() {
        return nodes.values().stream();
    }

    public Node<View<? extends BPMNViewDefinition>,
            Edge<ViewConnector<BPMNViewDefinition>,
                    Node<? extends View<? extends BPMNViewDefinition>, ?>>> getNode(String id) {
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

    public Stream<Edge<ViewConnector<BPMNViewDefinition>, Node<? extends View<? extends BPMNViewDefinition>, ?>>> edges() {
        return nodes().flatMap(e -> Stream.concat(e.getInEdges().stream(), e.getOutEdges().stream())).distinct().filter(e -> !(e.getContent() instanceof Child));
    }
}
