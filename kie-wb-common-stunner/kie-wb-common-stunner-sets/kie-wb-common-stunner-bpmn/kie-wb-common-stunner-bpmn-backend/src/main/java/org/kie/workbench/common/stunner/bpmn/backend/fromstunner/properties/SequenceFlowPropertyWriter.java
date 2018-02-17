package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import java.util.Optional;

import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.SequenceFlow;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.core.graph.content.view.Connection;
import org.kie.workbench.common.stunner.core.graph.content.view.DiscreteConnection;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;

public class SequenceFlowPropertyWriter extends PropertyWriter {

    private final SequenceFlow sequenceFlow;

    public SequenceFlowPropertyWriter(SequenceFlow sequenceFlow) {
        super(sequenceFlow);
        this.sequenceFlow = sequenceFlow;
    }

    public void setAutoConnectionSource(Connection connection) {
        DiscreteConnection c = (DiscreteConnection) connection;
        setMeta("isAutoConnection.source", Boolean.toString(c.isAuto()));
    }

    public void setAutoConnectionTarget(Connection connection) {
        DiscreteConnection c = (DiscreteConnection) connection;
        setMeta("isAutoConnection.target", Boolean.toString(c.isAuto()));
    }

    public void setAutoConnection(ViewConnector<?> content) {
        Optional<Connection> sourceConnection = content.getSourceConnection();
        setAutoConnectionSource(sourceConnection.get());

        Optional<Connection> targetConnection = content.getTargetConnection();
        setAutoConnectionTarget(targetConnection.get());
    }

    public void setSource(PropertyWriter pSrc) {
        sequenceFlow.setSourceRef((FlowNode) pSrc.getFlowElement());
        pSrc.setTarget(this);
    }

    public void setTarget(PropertyWriter pTgt) {
        sequenceFlow.setTargetRef((FlowNode) pTgt.getFlowElement());
        pTgt.setSource(this);
    }
}
