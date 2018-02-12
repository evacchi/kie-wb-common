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
import org.eclipse.bpmn2.Error;
import org.eclipse.bpmn2.ErrorEventDefinition;
import org.eclipse.bpmn2.Message;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.Signal;
import org.eclipse.bpmn2.SignalEventDefinition;
import org.eclipse.bpmn2.TerminateEventDefinition;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeMatch;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.DefinitionsBuildingContext;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Ids;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.Attributes;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.EndEventPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.BaseEndEvent;
import org.kie.workbench.common.stunner.bpmn.definition.EndErrorEvent;
import org.kie.workbench.common.stunner.bpmn.definition.EndMessageEvent;
import org.kie.workbench.common.stunner.bpmn.definition.EndNoneEvent;
import org.kie.workbench.common.stunner.bpmn.definition.EndSignalEvent;
import org.kie.workbench.common.stunner.bpmn.definition.EndTerminateEvent;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.error.ErrorEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.message.MessageEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.signal.ScopedSignalEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class EndEventConverter {

    private final DefinitionsBuildingContext context;

    public EndEventConverter(DefinitionsBuildingContext context) {
        this.context = context;
    }

    public PropertyWriter toFlowElement(Node<View<BaseEndEvent>, ?> node) {
        return NodeMatch.fromNode(BaseEndEvent.class, PropertyWriter.class)
                .when(EndNoneEvent.class, n -> {
                    EndEvent endEvent = bpmn2.createEndEvent();

                    BaseEndEvent definition = n.getContent().getDefinition();
                    EndEventPropertyWriter p = new EndEventPropertyWriter(endEvent);

                    endEvent.setId(n.getUUID());

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    return p;
                })
                .when(EndMessageEvent.class, n -> {
                    EndEvent endEvent = bpmn2.createEndEvent();
                    MessageEventDefinition messageEventDefinition =
                            bpmn2.createMessageEventDefinition();

                    EndMessageEvent definition = n.getContent().getDefinition();
                    EndEventPropertyWriter p = new EndEventPropertyWriter(endEvent);

                    endEvent.setId(n.getUUID());

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    p.setAssignmentsInfo(
                            definition.getDataIOSet().getAssignmentsinfo());

                    MessageEventExecutionSet executionSet = definition.getExecutionSet();
                    Message message = bpmn2.createMessage();
                    message.setName(executionSet.getMessageRef().getValue());
                    messageEventDefinition.setMessageRef(message);

                    endEvent.getEventDefinitions().add(messageEventDefinition);

                    p.addBaseElement(message);

                    return p;
                })
                .when(EndSignalEvent.class, n -> {
                    EndEvent endEvent = bpmn2.createEndEvent();
                    SignalEventDefinition messageEventDefinition =
                            bpmn2.createSignalEventDefinition();

                    EndSignalEvent definition = n.getContent().getDefinition();
                    EndEventPropertyWriter p = new EndEventPropertyWriter(endEvent);

                    endEvent.setId(n.getUUID());

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    p.setAssignmentsInfo(
                            definition.getDataIOSet().getAssignmentsinfo());

                    ScopedSignalEventExecutionSet executionSet =
                            definition.getExecutionSet();
                    Signal signal = bpmn2.createSignal();
                    String name = executionSet.getSignalRef().getValue();
                    signal.setName(name);
                    signal.setId(Ids.fromString(name));
                    messageEventDefinition.setSignalRef(signal.getId());

                    endEvent.getEventDefinitions().add(messageEventDefinition);

                    p.addBaseElement(signal);

                    return p;
                })
                .when(EndTerminateEvent.class, n -> {
                    EndEvent endEvent = bpmn2.createEndEvent();
                    TerminateEventDefinition terminateEventDefinition =
                            bpmn2.createTerminateEventDefinition();

                    EndTerminateEvent definition = n.getContent().getDefinition();
                    EndEventPropertyWriter p = new EndEventPropertyWriter(endEvent);

                    endEvent.setId(n.getUUID());

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    endEvent.getEventDefinitions().add(terminateEventDefinition);

                    return p;
                })
                .when(EndErrorEvent.class, n -> {
                    EndEvent endEvent = bpmn2.createEndEvent();
                    ErrorEventDefinition errorEventDefinition =
                            bpmn2.createErrorEventDefinition();

                    EndErrorEvent definition = n.getContent().getDefinition();
                    EndEventPropertyWriter p = new EndEventPropertyWriter(endEvent);

                    endEvent.setId(n.getUUID());

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());

                    ErrorEventExecutionSet executionSet = definition.getExecutionSet();
                    Error error = bpmn2.createError();
                    String errorCode = executionSet.getErrorRef().getValue();
                    error.setId(errorCode);
                    error.setErrorCode(errorCode);
                    errorEventDefinition.setErrorRef(error);

                    FeatureMap.Entry erefname =
                            Attributes.drools("erefname", errorCode);
                    errorEventDefinition.getAnyAttribute().add(erefname);

                    endEvent.getEventDefinitions().add(errorEventDefinition);

                    p.addBaseElement(error);

                    return p;
                })

                .apply(node).value();
    }
}
