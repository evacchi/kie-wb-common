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

package org.kie.workbench.common.stunner.bpmn.backend.converters.properties;

import java.util.List;
import java.util.Optional;

import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.di.BPMNEdge;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.dd.dc.Bounds;
import org.eclipse.dd.dc.Point;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ScriptTypeValue;
import org.kie.workbench.common.stunner.core.graph.content.view.Connection;
import org.kie.workbench.common.stunner.core.graph.content.view.MagnetConnection;
import org.kie.workbench.common.stunner.core.graph.content.view.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenceFlowPropertyReader extends BasePropertyReader {

    private static final Logger logger = LoggerFactory.getLogger(SequenceFlowPropertyReader.class);
    final FormalExpression conditionExpression;
    private final SequenceFlow seq;

    public SequenceFlowPropertyReader(SequenceFlow seq, BPMNPlane plane) {
        super(seq, plane);
        this.seq = seq;
        conditionExpression = (FormalExpression) seq.getConditionExpression();
    }

    public String getPriority() {
        return attribute("priority");
    }

    public boolean isAutoConnectionSource() {
        return Boolean.parseBoolean(metaData("isAutoConnection.source"));
    }

    public boolean isAutoConnectionTarget() {
        return Boolean.parseBoolean(metaData("isAutoConnection.target"));
    }

    public ScriptTypeValue getConditionExpression() {
        if (conditionExpression == null) {
            return new ScriptTypeValue();
        } else {
            return new ScriptTypeValue(
                    Scripts.scriptLanguageFromUri(conditionExpression.getLanguage()),
                    conditionExpression.getBody());
        }
    }

    public String getConditionExpressionLanguage() {
        return conditionExpression == null ?
                "" :
                Scripts.scriptLanguageFromUri(conditionExpression.getLanguage());
    }

    public String getSourceId() {
        return seq.getSourceRef().getId();
    }

    public String getTargetId() {
        return seq.getTargetRef().getId();
    }

    public Connection getSourceConnection() {
        Point2D sourcePosition = getSourcePosition(element.getId(), getSourceId());
        return MagnetConnection.Builder.at(sourcePosition.getX(), sourcePosition.getY()).setAuto(isAutoConnectionSource());
    }

    public Connection getTargetConnection() {
        Point2D targetPosition = getTargetPosition(element.getId(), getTargetId());
        return MagnetConnection.Builder.at(targetPosition.getX(), targetPosition.getY()).setAuto(isAutoConnectionTarget());
    }

    private Point2D getSourcePosition(String edgeId, String sourceId) {
        BPMNEdge bpmnEdge = getBPMNEdgeForElement(edgeId).get();
        Bounds sourceBounds = getShape(plane, sourceId).getBounds();
        List<Point> waypoint = bpmnEdge.getWaypoint();
        return waypoint.isEmpty() ?
                sourcePosition(sourceBounds)
                : offsetPosition(sourceBounds, waypoint.get(0));
    }

    private Point2D getTargetPosition(String edgeId, String targetId) {
        BPMNEdge bpmnEdge = getBPMNEdgeForElement(edgeId).get();
        Bounds targetBounds = getShape(plane, targetId).getBounds();
        List<Point> waypoint = bpmnEdge.getWaypoint();

        if (waypoint.size() > 2) {
            logger.warn("Waypoints should be either 0 or 2. Unexpected size: " + waypoint.size());
        }

        return waypoint.isEmpty() ?
                targetPosition(targetBounds)
                : offsetPosition(targetBounds, waypoint.get(waypoint.size() - 1));
    }

    private Point2D sourcePosition(Bounds sourceBounds) {
        return Point2D.create(sourceBounds.getWidth(),
                              sourceBounds.getHeight() / 2);
    }

    private Point2D offsetPosition(Bounds sourceBounds, Point wayPoint) {
        return Point2D.create(wayPoint.getX() - sourceBounds.getX(),
                              wayPoint.getY() - sourceBounds.getY());
    }

    private Point2D targetPosition(Bounds targetBounds) {
        return Point2D.create(0,
                              targetBounds.getHeight() / 2);
    }

    private Optional<BPMNEdge> getBPMNEdgeForElement(String elementId) {
        return plane.getPlaneElement().stream()
                .filter(dia -> dia instanceof BPMNEdge)
                .map(edge -> (BPMNEdge) edge)
                .filter(edge -> edge.getBpmnElement().getId().equals(elementId))
                .findFirst();
    }
}
