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

package org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.kie.workbench.common.stunner.bpmn.BPMNDefinitionSet;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.VoidMatch;
import org.kie.workbench.common.stunner.core.api.DefinitionManager;
import org.kie.workbench.common.stunner.core.command.Command;
import org.kie.workbench.common.stunner.core.command.CommandResult;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Element;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.command.EmptyRulesCommandExecutionContext;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandExecutionContext;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandManager;
import org.kie.workbench.common.stunner.core.graph.command.impl.AddChildNodeCommand;
import org.kie.workbench.common.stunner.core.graph.command.impl.AddDockedNodeCommand;
import org.kie.workbench.common.stunner.core.graph.command.impl.AddNodeCommand;
import org.kie.workbench.common.stunner.core.graph.command.impl.GraphCommandFactory;
import org.kie.workbench.common.stunner.core.graph.command.impl.SetConnectionSourceNodeCommand;
import org.kie.workbench.common.stunner.core.graph.command.impl.SetConnectionTargetNodeCommand;
import org.kie.workbench.common.stunner.core.graph.command.impl.UpdateElementPositionCommand;
import org.kie.workbench.common.stunner.core.graph.content.Bounds;
import org.kie.workbench.common.stunner.core.graph.content.definition.DefinitionSet;
import org.kie.workbench.common.stunner.core.graph.content.view.BoundsImpl;
import org.kie.workbench.common.stunner.core.graph.content.view.Connection;
import org.kie.workbench.common.stunner.core.graph.content.view.Point2D;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.processing.index.map.MapIndexBuilder;
import org.kie.workbench.common.stunner.core.rule.RuleManager;
import org.kie.workbench.common.stunner.core.rule.RuleViolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper for graph command execution,
 * exposing a simple, method-based API
 */
public class GraphBuildingContext {

    private static final Logger logger = LoggerFactory.getLogger(GraphBuildingContext.class);

    private final GraphCommandExecutionContext executionContext;
    private final GraphCommandFactory commandFactory;
    private final GraphCommandManager commandManager;
    private final Graph<DefinitionSet, Node> graph;

    public GraphBuildingContext(
            Graph<DefinitionSet, Node> graph,
            DefinitionManager definitionManager,
            TypedFactoryManager typedFactoryManager,
            RuleManager ruleManager,
            GraphCommandFactory commandFactory,
            GraphCommandManager commandManager) {
        this.graph = graph;
        this.executionContext = new EmptyRulesCommandExecutionContext(
                definitionManager,
                typedFactoryManager.untyped(),
                ruleManager,
                new MapIndexBuilder().build(graph));
        this.commandFactory = commandFactory;
        this.commandManager = commandManager;
    }

    /**
     * Clears the context and then walks the graph root
     * to draw it on the canvas
     */
    public void render(BpmnNode root) {
        clearGraph();
        buildGraph(root);
    }

    /**
     * Starting from the given root node,
     * it walks the graph breadth-first and issues
     * all the required commands to draw it on the canvas
     */
    public void buildGraph(BpmnNode rootNode) {
        this.addNode(rootNode.value());
        rootNode.getEdges().forEach(this::addEdge);
        Deque<BpmnNode> workingSet = new ArrayDeque<>(rootNode.getChildren());
        Set<BpmnNode> workedOff = new HashSet<>();
        while (!workingSet.isEmpty()) {
            BpmnNode current = workingSet.pop();
            // ensure we visit this node only once
            if (workedOff.contains(current)) {
                continue;
            }
            workedOff.add(current);
            workingSet.addAll(current.getChildren());
            logger.debug("{} :: {}",
                         current.getParent().value().getUUID(),
                         current.value().getUUID());

            this.addChildNode(current.getParent().value(), current.value());
            current.getEdges().forEach(this::addEdge);
        }
    }

    public void addDockedNode(String parentId, String candidateId) {
        Node parent = executionContext.getGraphIndex().getNode(parentId);
        Node candidate = executionContext.getGraphIndex().getNode(candidateId);

        addDockedNode(parent, candidate);
    }

    private void addDockedNode(Node parent, Node candidate) {
        AddDockedNodeCommand addNodeCommand = commandFactory.addDockedNode(parent, candidate);
        execute(addNodeCommand);
    }

    public void addChildNode(String parentId, String childId) {
        Node parent = getNode(parentId);
        Node child = executionContext.getGraphIndex().getNode(childId);

        AddChildNodeCommand addChildNodeCommand = commandFactory.addChildNode(parent, child);
        execute(addChildNodeCommand);
    }

    public Node getNode(String id) {
        return executionContext.getGraphIndex().getNode(id);
    }

    public void addChildNode(Node<? extends View, ?> parent, Node<? extends View, ?> child) {
        AddChildNodeCommand addChildNodeCommand = commandFactory.addChildNode(parent, child);
        execute(addChildNodeCommand);

        translate(child, parent.getContent().getBounds().getUpperLeft());
    }

    /**
     * Move node into a new coordinate system with origin in newOrigin.
     * <p>
     * E.g., assume origin is currently (0,0), and consider node at (10,11).
     * If we move node into a new coordinate system where the origin is in (3, 4)
     * then the new coordinates for node are: (10-3, 11-4) = (7,7)
     */
    public void translate(Node<? extends View, ?> node, Bounds.Bound newOrigin) {

        logger.debug("Translating {} into constraints {}", node.getContent().getBounds(), newOrigin);

        Bounds childBounds = node.getContent().getBounds();
        double constrainedX = childBounds.getUpperLeft().getX() - newOrigin.getX();
        double constrainedY = childBounds.getUpperLeft().getY() - newOrigin.getY();

        Point2D coords = Point2D.create(constrainedX, constrainedY);
        updatePosition(node, coords);
    }

    public void updatePosition(Node node, Point2D position) {
        UpdateElementPositionCommand updateElementPositionCommand =
                commandFactory.updatePosition(node, position);
        execute(updateElementPositionCommand);
    }

    public void addNode(Node node) {
        AddNodeCommand addNodeCommand = commandFactory.addNode(node);
        execute(addNodeCommand);
    }

    public void addEdge(
            Edge<? extends View<?>, Node> edge,
            Node source,
            Connection sourceConnection,
            Node target,
            Connection targetConnection) {
        SetConnectionSourceNodeCommand setSourceNode =
                commandFactory.setSourceNode(source, edge, sourceConnection);

        SetConnectionTargetNodeCommand setTargetNode =
                commandFactory.setTargetNode(target, edge, targetConnection);

        execute(setSourceNode);
        execute(setTargetNode);
    }

    public void addEdge(
            Edge<? extends View<?>, Node> edge,
            String sourceId,
            Connection sourceConnection,
            String targetId,
            Connection targetConnection) {

        Node source = executionContext.getGraphIndex().getNode(sourceId);
        Node target = executionContext.getGraphIndex().getNode(targetId);

        Objects.requireNonNull(source);
        Objects.requireNonNull(target);

        addEdge(edge, source, sourceConnection, target, targetConnection);
    }

    public void setBounds(String elementId, int x1, int y1, int x2, int y2) {
        Element<? extends View<?>> element = executionContext.getGraphIndex().get(elementId);
        element.getContent().setBounds(BoundsImpl.build(x1, y1, x2, y2));
    }

    private CommandResult<RuleViolation> execute(Command<GraphCommandExecutionContext, RuleViolation> command) {
        return commandManager.execute(executionContext, command);
    }

    public GraphCommandExecutionContext executionContext() {
        return executionContext;
    }

    public CommandResult<RuleViolation> clearGraph() {
        return commandManager.execute(executionContext, commandFactory.clearGraph());
    }

    public void addEdge(BpmnEdge edge) {
        VoidMatch.of(BpmnEdge.class)
                .when(BpmnEdge.Simple.class, e ->
                        addEdge(e.getEdge(),
                                e.getSource().value(),
                                e.getSourceConnection(),
                                e.getTarget().value(),
                                e.getTargetConnection())
                )
                .when(BpmnEdge.Docked.class, e ->
                        addDockedNode(e.getSource().value(),
                                      e.getTarget().value())
                ).apply(edge);
    }
}
