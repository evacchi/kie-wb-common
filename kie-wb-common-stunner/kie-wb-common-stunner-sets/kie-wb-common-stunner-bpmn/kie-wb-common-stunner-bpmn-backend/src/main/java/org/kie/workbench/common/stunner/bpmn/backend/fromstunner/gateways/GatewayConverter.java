package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.gateways;

import org.eclipse.bpmn2.ExclusiveGateway;
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeMatch;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.BaseGateway;
import org.kie.workbench.common.stunner.bpmn.definition.ExclusiveDatabasedGateway;
import org.kie.workbench.common.stunner.bpmn.definition.ParallelGateway;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class GatewayConverter {

    public PropertyWriter toFlowElement(Node<View<BaseGateway>, ?> node) {
        return NodeMatch.fromNode(BaseGateway.class, PropertyWriter.class)
                .when(ParallelGateway.class, n -> {

                    org.eclipse.bpmn2.ParallelGateway gateway = bpmn2.createParallelGateway();
                    PropertyWriter p = new PropertyWriter(gateway);
                    gateway.setId(n.getUUID());

                    p.setBounds(n.getContent().getBounds());

                    return p;
                })
                .when(ExclusiveDatabasedGateway.class, n -> {

                    ExclusiveGateway gateway = bpmn2.createExclusiveGateway();
                    PropertyWriter p = new PropertyWriter(gateway);
                    gateway.setId(n.getUUID());

                    p.setBounds(n.getContent().getBounds());

                    return p;
                }).apply(node).value();
    }
}
