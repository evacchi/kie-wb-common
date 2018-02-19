package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.gateways;

import java.util.List;

import org.eclipse.bpmn2.GatewayDirection;
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeMatch;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.GatewayPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.BaseGateway;
import org.kie.workbench.common.stunner.bpmn.definition.ExclusiveGateway;
import org.kie.workbench.common.stunner.bpmn.definition.ParallelGateway;
import org.kie.workbench.common.stunner.bpmn.definition.property.gateway.GatewayExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class GatewayConverter {

    public PropertyWriter toFlowElement(Node<View<BaseGateway>, ?> node) {
        return NodeMatch.fromNode(BaseGateway.class, PropertyWriter.class)
                .when(ParallelGateway.class, n -> {

                    org.eclipse.bpmn2.ParallelGateway gateway = bpmn2.createParallelGateway();
                    GatewayPropertyWriter p = new GatewayPropertyWriter(gateway);
                    gateway.setId(n.getUUID());

                    ParallelGateway definition = n.getContent().getDefinition();

                    p.setGatewayDirection(n);

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    p.setBounds(n.getContent().getBounds());

                    return p;
                })
                .when(ExclusiveGateway.class, n -> {

                    org.eclipse.bpmn2.ExclusiveGateway gateway = bpmn2.createExclusiveGateway();
                    GatewayPropertyWriter p = new GatewayPropertyWriter(gateway);
                    gateway.setId(n.getUUID());

                    ExclusiveGateway definition = n.getContent().getDefinition();

                    p.setGatewayDirection(n);

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    GatewayExecutionSet executionSet = definition.getExecutionSet();
                    p.setDefaultRoute(executionSet.getDefaultRoute().getValue());

                    p.setBounds(n.getContent().getBounds());

                    return p;
                }).apply(node).value();
    }


}
