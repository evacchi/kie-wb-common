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
import org.eclipse.bpmn2.TerminateEventDefinition;
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeMatch;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.DefinitionsBuildingContext;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.ThrowEventPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.BaseEndEvent;
import org.kie.workbench.common.stunner.bpmn.definition.EndErrorEvent;
import org.kie.workbench.common.stunner.bpmn.definition.EndMessageEvent;
import org.kie.workbench.common.stunner.bpmn.definition.EndNoneEvent;
import org.kie.workbench.common.stunner.bpmn.definition.EndSignalEvent;
import org.kie.workbench.common.stunner.bpmn.definition.EndTerminateEvent;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.error.ErrorEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.message.MessageEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class EndEventConverter {

    public PropertyWriter toFlowElement(Node<View<BaseEndEvent>, ?> node) {


        return NodeMatch.fromNode(BaseEndEvent.class, PropertyWriter.class)
                .when(EndNoneEvent.class, n -> {
                    EndEvent event = bpmn2.createEndEvent();
                    event.setId(node.getUUID());

                    BaseEndEvent definition = n.getContent().getDefinition();
                    ThrowEventPropertyWriter p = new ThrowEventPropertyWriter(event);

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    p.setBounds(n.getContent().getBounds());
                    return p;
                })
                .when(EndMessageEvent.class, n -> {
                    EndEvent event = bpmn2.createEndEvent();
                    event.setId(node.getUUID());

                    EndMessageEvent definition = n.getContent().getDefinition();
                    ThrowEventPropertyWriter p = new ThrowEventPropertyWriter(event);

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    p.setAssignmentsInfo(
                            definition.getDataIOSet().getAssignmentsinfo());

                    MessageEventExecutionSet executionSet =
                            definition.getExecutionSet();

                    p.addMessage(executionSet.getMessageRef());

                    p.setBounds(n.getContent().getBounds());
                    return p;
                })
                .when(EndSignalEvent.class, n -> {
                    EndEvent event = bpmn2.createEndEvent();
                    event.setId(node.getUUID());

                    EndSignalEvent definition = n.getContent().getDefinition();
                    ThrowEventPropertyWriter p = new ThrowEventPropertyWriter(event);

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    p.setAssignmentsInfo(
                            definition.getDataIOSet().getAssignmentsinfo());

                    p.addSignal(definition.getExecutionSet().getSignalRef());

                    p.setBounds(n.getContent().getBounds());
                    return p;
                })
                .when(EndTerminateEvent.class, n -> {
                    EndEvent event = bpmn2.createEndEvent();
                    event.setId(node.getUUID());

                    EndTerminateEvent definition = n.getContent().getDefinition();
                    ThrowEventPropertyWriter p = new ThrowEventPropertyWriter(event);

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    p.addTerminate();

                    p.setBounds(n.getContent().getBounds());
                    return p;
                })
                .when(EndErrorEvent.class, n -> {
                    EndEvent event = bpmn2.createEndEvent();
                    event.setId(node.getUUID());

                    EndErrorEvent definition = n.getContent().getDefinition();
                    ThrowEventPropertyWriter p = new ThrowEventPropertyWriter(event);

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    p.setAssignmentsInfo(
                            definition.getDataIOSet().getAssignmentsinfo());

                    ErrorEventExecutionSet executionSet = definition.getExecutionSet();
                    p.addError(executionSet.getErrorRef());

                    p.setBounds(n.getContent().getBounds());
                    return p;
                })
                .apply(node).value();
    }
}
