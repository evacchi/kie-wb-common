package org.kie.workbench.common.stunner.bpmn.backend.converters;

import org.kie.workbench.common.stunner.bpmn.definition.SequenceFlow;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.Connection;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public class BpmnEdge {

    private final Edge<View<SequenceFlow>, Node> edge;
    private final String sourceId;
    private final Connection sourceConnection;
    private final String targetId;
    private final Connection targetConnection;

    public BpmnEdge(Edge<View<SequenceFlow>, Node> edge, String sourceId, Connection sourceConnection, String targetId, Connection targetConnection) {

        this.edge = edge;
        this.sourceId = sourceId;
        this.sourceConnection = sourceConnection;
        this.targetId = targetId;
        this.targetConnection = targetConnection;
    }

    public static BpmnEdge of(Edge<View<SequenceFlow>, Node> edge, String sourceId, Connection sourceConnection, String targetId, Connection targetConnection) {
        return new BpmnEdge(edge, sourceId, sourceConnection, targetId, targetConnection);
    }

    public Edge<View<SequenceFlow>, Node> getEdge() {
        return edge;
    }

    public String getSourceId() {
        return sourceId;
    }

    public Connection getSourceConnection() {
        return sourceConnection;
    }

    public String getTargetId() {
        return targetId;
    }

    public Connection getTargetConnection() {
        return targetConnection;
    }
}
