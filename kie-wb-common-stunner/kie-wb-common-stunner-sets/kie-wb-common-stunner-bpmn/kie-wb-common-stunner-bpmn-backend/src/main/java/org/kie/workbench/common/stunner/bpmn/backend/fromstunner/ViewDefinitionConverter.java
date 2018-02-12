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

import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.di.BPMNEdge;
import org.eclipse.bpmn2.di.BPMNShape;
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeMatch;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.events.EndEventConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.events.StartEventConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.tasks.TaskConverter;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.bpmn.definition.BaseEndEvent;
import org.kie.workbench.common.stunner.bpmn.definition.BaseStartEvent;
import org.kie.workbench.common.stunner.bpmn.definition.BaseTask;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.Bounds;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.dc;
import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.di;

public class ViewDefinitionConverter {

    private final DefinitionsBuildingContext context;
    private final StartEventConverter startEventConverter;
    private final TaskConverter taskConverter;
    private final EndEventConverter endEventConverter;

    public ViewDefinitionConverter(DefinitionsBuildingContext context) {
        this.context = context;
        this.startEventConverter = new StartEventConverter();
        this.endEventConverter = new EndEventConverter();
        this.taskConverter = new TaskConverter();
    }

    public Result<PropertyWriter> toFlowElement(Node<View<? extends BPMNViewDefinition>, ?> node) {
        return NodeMatch.fromNode(BPMNViewDefinition.class, PropertyWriter.class)
                .when(BaseStartEvent.class, startEventConverter::toFlowElement)
                .when(BaseTask.class, taskConverter::toFlowElement)
                .when(BaseEndEvent.class, endEventConverter::toFlowElement)
                .apply(node);
    }

    @Deprecated
    public BPMNShape shapeFrom(Node<View<? extends BPMNViewDefinition>, ?> node) {
        FlowNode element = context.getFlowNode(node.getUUID());
        PropertyWriter p = new PropertyWriter(element);
        return p.getShape();
    }

    public BPMNEdge edgeFrom(
            Edge<? extends ViewConnector<? extends BPMNViewDefinition>,
                    Node<? extends View<? extends BPMNViewDefinition>, ?>> edge) {

        org.eclipse.bpmn2.SequenceFlow element = context.getSequenceFlow(edge.getUUID());

        BPMNEdge bpmnEdge = di.createBPMNEdge();
        bpmnEdge.setBpmnElement(element);

        Bounds.Bound sourceUpperLeft = edge.getSourceNode().getContent().getBounds().getUpperLeft();
        org.eclipse.dd.dc.Point sourcePoint = dc.createPoint();
        sourcePoint.setX(sourceUpperLeft.getX().floatValue());
        sourcePoint.setY(sourceUpperLeft.getY().floatValue());

        Bounds.Bound targetUpperLeft = edge.getTargetNode().getContent().getBounds().getUpperLeft();
        org.eclipse.dd.dc.Point targetPoint = dc.createPoint();
        targetPoint.setX(targetUpperLeft.getX().floatValue());
        targetPoint.setY(targetUpperLeft.getY().floatValue());

        bpmnEdge.getWaypoint().add(sourcePoint);
        bpmnEdge.getWaypoint().add(targetPoint);

        return bpmnEdge;
    }
}

