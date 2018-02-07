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

import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.di.BPMNEdge;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.bpmn2.di.BpmnDiFactory;
import org.eclipse.dd.dc.DcFactory;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.Bounds;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;

public class BPMNDiagramUnconverter {

    private static final BpmnDiFactory di = BpmnDiFactory.eINSTANCE;
    private static final DcFactory dc = DcFactory.eINSTANCE;

    private final UnconverterContext context;

    public BPMNDiagramUnconverter(UnconverterContext context) {
        this.context = context;
    }

    public BPMNShape shapeFrom(
            Node<View<? extends BPMNViewDefinition>, ?> node) {

        FlowNode element = context.getFlowNode(node.getUUID());

        BPMNShape shape = di.createBPMNShape();
        shape.setBpmnElement(element);
        Bounds.Bound upperLeft = node.getContent().getBounds().getUpperLeft();
        Bounds.Bound lowerRight = node.getContent().getBounds().getLowerRight();
        org.eclipse.dd.dc.Bounds bounds = dc.createBounds();
        bounds.setX(upperLeft.getX().floatValue());
        bounds.setY(upperLeft.getY().floatValue());
        bounds.setWidth(lowerRight.getX().floatValue() - upperLeft.getX().floatValue());
        bounds.setHeight(lowerRight.getY().floatValue() - upperLeft.getY().floatValue());
        shape.setBounds(bounds);
        return shape;
    }

    public BPMNEdge edgeFrom(
            Edge<? extends ViewConnector<? extends BPMNViewDefinition>, Node<? extends View<? extends BPMNViewDefinition>, ?>> edge) {

        FlowNode element = context.getFlowNode(edge.getUUID());

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
