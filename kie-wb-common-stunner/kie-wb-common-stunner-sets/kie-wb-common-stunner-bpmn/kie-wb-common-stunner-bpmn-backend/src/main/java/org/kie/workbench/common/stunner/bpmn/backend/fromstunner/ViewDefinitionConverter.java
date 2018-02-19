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

import java.util.Map;

import org.eclipse.bpmn2.di.BPMNEdge;
import org.eclipse.bpmn2.di.BPMNShape;
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeMatch;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.events.EndEventConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.events.IntermediateCatchEventConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.events.IntermediateThrowEventConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.events.StartEventConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.gateways.GatewayConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.lanes.LaneConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.BasePropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.tasks.TaskConverter;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.bpmn.definition.BaseCatchingIntermediateEvent;
import org.kie.workbench.common.stunner.bpmn.definition.BaseEndEvent;
import org.kie.workbench.common.stunner.bpmn.definition.BaseGateway;
import org.kie.workbench.common.stunner.bpmn.definition.BaseStartEvent;
import org.kie.workbench.common.stunner.bpmn.definition.BaseTask;
import org.kie.workbench.common.stunner.bpmn.definition.BaseThrowingIntermediateEvent;
import org.kie.workbench.common.stunner.bpmn.definition.Lane;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.Bounds;
import org.kie.workbench.common.stunner.core.graph.content.view.Connection;
import org.kie.workbench.common.stunner.core.graph.content.view.DiscreteConnection;
import org.kie.workbench.common.stunner.core.graph.content.view.Point2D;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.dc;
import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.di;

public class ViewDefinitionConverter {

    private final StartEventConverter startEventConverter;
    private final TaskConverter taskConverter;
    private final EndEventConverter endEventConverter;
    private final IntermediateCatchEventConverter intermediateCatchEventConverter;
    private final IntermediateThrowEventConverter intermediateThrowEventConverter;
    private GatewayConverter gatewayConverter;

    public ViewDefinitionConverter() {
        this.startEventConverter = new StartEventConverter();
        this.endEventConverter = new EndEventConverter();
        this.intermediateCatchEventConverter = new IntermediateCatchEventConverter();
        this.intermediateThrowEventConverter = new IntermediateThrowEventConverter();
        this.gatewayConverter = new GatewayConverter();
        this.taskConverter = new TaskConverter();
    }

    public Result<PropertyWriter> toFlowElement(Node<View<? extends BPMNViewDefinition>, ?> node) {
        return NodeMatch.fromNode(BPMNViewDefinition.class, PropertyWriter.class)
                .when(BaseStartEvent.class, startEventConverter::toFlowElement)
                .when(BaseCatchingIntermediateEvent.class, intermediateCatchEventConverter::toFlowElement)
                .when(BaseThrowingIntermediateEvent.class, intermediateThrowEventConverter::toFlowElement)
                .when(BaseEndEvent.class, endEventConverter::toFlowElement)
                .when(BaseTask.class, taskConverter::toFlowElement)
                .when(BaseGateway.class, gatewayConverter::toFlowElement)
                .ignore(Lane.class)
                .apply(node);
    }
//
//    public BPMNEdge edgeFrom(
//            Map<String, BasePropertyWriter> props,
//            Edge<? extends ViewConnector<? extends BPMNViewDefinition>,
//                    Node<? extends View<? extends BPMNViewDefinition>, ?>> edge) {
//
//        org.eclipse.bpmn2.SequenceFlow element = context.getSequenceFlow(edge.getUUID());
//
//        BPMNEdge bpmnEdge = di.createBPMNEdge();
//        bpmnEdge.setBpmnElement(element);
//
//        BasePropertyWriter sourcePropertyWriter = props.get(element.getSourceRef().getId());
//        BPMNShape sourceShape = sourcePropertyWriter.getShape();
//        bpmnEdge.setSourceElement(sourceShape);
//
//        BasePropertyWriter targetPropertyWriter = props.get(element.getTargetRef().getId());
//        BPMNShape targetShape = targetPropertyWriter.getShape();
//        bpmnEdge.setTargetElement(targetShape);
//
//        ViewConnector<? extends BPMNViewDefinition> content = edge.getContent();
//        Point2D sourcePt = content.getSourceConnection().get().getLocation();
//        Point2D targetPt = content.getTargetConnection().get().getLocation();
//
//        org.eclipse.dd.dc.Point sourcePoint = dc.createPoint();
//        sourcePoint.setX(
//                sourceShape.getBounds().getX() + (float) sourcePt.getX());
//        sourcePoint.setY(
//                sourceShape.getBounds().getY() + (float) sourcePt.getY());
//
//        org.eclipse.dd.dc.Point targetPoint = dc.createPoint();
//        targetPoint.setX(
//                targetShape.getBounds().getX() + (float) targetPt.getX());
//        targetPoint.setY(
//                targetShape.getBounds().getY() + (float) targetPt.getY());
//
////        Bounds.Bound sourceUpperLeft = edge.getSourceNode().getContent().getBounds().getUpperLeft();
////        sourcePoint.setX(sourceUpperLeft.getX().floatValue() + sourceShape.getBounds().getWidth()/2);
////        sourcePoint.setY(sourceUpperLeft.getY().floatValue());
////
////        Bounds.Bound targetUpperLeft = edge.getTargetNode().getContent().getBounds().getUpperLeft();
////        org.eclipse.dd.dc.Point targetPoint = dc.createPoint();
////        targetPoint.setX(targetUpperLeft.getX().floatValue());
////        targetPoint.setY(targetUpperLeft.getY().floatValue() +  targetShape.getBounds().getHeight()/2);
//
//        bpmnEdge.getWaypoint().add(sourcePoint);
//        bpmnEdge.getWaypoint().add(targetPoint);
//
//        return bpmnEdge;
//    }
}

