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

package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.events;

import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Message;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeMatch;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.DefinitionsBuildingContext;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.BaseEndEvent;
import org.kie.workbench.common.stunner.bpmn.definition.EndMessageEvent;
import org.kie.workbench.common.stunner.bpmn.definition.EndNoneEvent;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.message.MessageEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class EndEventConverter {

    private final DefinitionsBuildingContext context;

    public EndEventConverter(DefinitionsBuildingContext context) {
        this.context = context;
    }

    public FlowNode toFlowElement(Node<View<BaseEndEvent>, ?> node) {
        return NodeMatch.fromNode(BaseEndEvent.class, FlowNode.class)
                .when(EndNoneEvent.class, n -> {
                    EndEvent endEvent = bpmn2.createEndEvent();

                    BaseEndEvent definition = n.getContent().getDefinition();
                    PropertyWriter p = new PropertyWriter(endEvent);

                    endEvent.setId(n.getUUID());

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    return endEvent;
                })
                .when(EndMessageEvent.class, n -> {
                    EndEvent endEvent = bpmn2.createEndEvent();
                    MessageEventDefinition messageEventDefinition =
                            bpmn2.createMessageEventDefinition();

                    EndMessageEvent definition = n.getContent().getDefinition();
                    PropertyWriter p = new PropertyWriter(endEvent);

                    endEvent.setId(n.getUUID());

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getName().getValue());

                    // fixme we must now parse this
                    AssignmentsInfo assignmentsinfo =
                            definition.getDataIOSet().getAssignmentsinfo();

                    MessageEventExecutionSet executionSet = definition.getExecutionSet();
                    Message message = bpmn2.createMessage();
                    message.setName(executionSet.getMessageRef().getValue());
                    messageEventDefinition.setMessageRef(message);

                    endEvent.getEventDefinitions().add(messageEventDefinition);

                    context.addRootElement(message);

                    return endEvent;
                }).apply(node).value();
    }
}
