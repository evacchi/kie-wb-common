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

import org.eclipse.bpmn2.IntermediateCatchEvent;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeMatch;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.CatchEventPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.ThrowEventPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.BaseThrowingIntermediateEvent;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateMessageEventThrowing;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateSignalEventThrowing;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.message.MessageEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class IntermediateThrowEventConverter {

    public PropertyWriter toFlowElement(Node<View<BaseThrowingIntermediateEvent>, ?> node) {
        return NodeMatch.fromNode(BaseThrowingIntermediateEvent.class, PropertyWriter.class)
                .when(IntermediateMessageEventThrowing.class, n -> {
                    IntermediateThrowEvent event = bpmn2.createIntermediateThrowEvent();
                    event.setId(node.getUUID());

                    IntermediateMessageEventThrowing definition = n.getContent().getDefinition();
                    ThrowEventPropertyWriter p = new ThrowEventPropertyWriter(event);

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    p.setAssignmentsInfo(
                            definition.getDataIOSet().getAssignmentsinfo());

                    MessageEventExecutionSet executionSet = definition.getExecutionSet();

                    p.addMessage(executionSet.getMessageRef());

                    p.setBounds(n.getContent().getBounds());
                    return p;
                })
                .when(IntermediateSignalEventThrowing.class, n -> {
                    IntermediateThrowEvent event = bpmn2.createIntermediateThrowEvent();
                    event.setId(node.getUUID());

                    IntermediateSignalEventThrowing definition = n.getContent().getDefinition();
                    ThrowEventPropertyWriter p = new ThrowEventPropertyWriter(event);

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    p.setAssignmentsInfo(
                            definition.getDataIOSet().getAssignmentsinfo());

                    p.addSignal(definition.getExecutionSet().getSignalRef());
                    p.addSignalScope(definition.getExecutionSet().getSignalScope());

                    p.setBounds(n.getContent().getBounds());
                    return p;
                })
                .apply(node).value();
    }
}