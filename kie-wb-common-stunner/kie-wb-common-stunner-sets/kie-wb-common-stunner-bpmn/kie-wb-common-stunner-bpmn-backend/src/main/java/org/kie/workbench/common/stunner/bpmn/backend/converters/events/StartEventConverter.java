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

package org.kie.workbench.common.stunner.bpmn.backend.converters.events;

import java.util.List;

import org.eclipse.bpmn2.CompensateEventDefinition;
import org.eclipse.bpmn2.ConditionalEventDefinition;
import org.eclipse.bpmn2.ErrorEventDefinition;
import org.eclipse.bpmn2.EscalationEventDefinition;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.SignalEventDefinition;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.TimerEventDefinition;
import org.kie.workbench.common.stunner.bpmn.backend.converters.DefinitionResolver;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Match;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.Properties;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.bpmn.definition.BaseStartEvent;
import org.kie.workbench.common.stunner.bpmn.definition.StartErrorEvent;
import org.kie.workbench.common.stunner.bpmn.definition.StartMessageEvent;
import org.kie.workbench.common.stunner.bpmn.definition.StartNoneEvent;
import org.kie.workbench.common.stunner.bpmn.definition.StartSignalEvent;
import org.kie.workbench.common.stunner.bpmn.definition.StartTimerEvent;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.IsInterrupting;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.error.ErrorRef;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.error.InterruptingErrorEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.message.InterruptingMessageEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.message.MessageRef;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.signal.InterruptingSignalEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.signal.SignalRef;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.timer.InterruptingTimerEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.Name;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public class StartEventConverter {

    private final TypedFactoryManager factoryManager;
    private final DefinitionResolver definitionResolver;

    public StartEventConverter(TypedFactoryManager factoryManager, DefinitionResolver definitionResolver) {
        this.factoryManager = factoryManager;
        this.definitionResolver = definitionResolver;
    }

    public Node<? extends View<? extends BPMNViewDefinition>, ?> convert(StartEvent startEvent) {
        List<EventDefinition> eventDefinitions = startEvent.getEventDefinitions();
        Node<? extends View<? extends BaseStartEvent>, ?> convertedStartEvent = convertStartEvent(startEvent, eventDefinitions);

        return convertedStartEvent;
    }

    private Node<? extends View<? extends BaseStartEvent>, ?> convertStartEvent(StartEvent event, List<EventDefinition> eventDefinitions) {
        String nodeId = event.getId();
        switch (eventDefinitions.size()) {
            case 0: {
                Node<View<StartNoneEvent>, Edge> node = factoryManager.newNode(nodeId, StartNoneEvent.class);
                StartNoneEvent definition = node.getContent().getDefinition();
                definition.setGeneral(new BPMNGeneralSet(
                        new Name(event.getName()),
                        Properties.documentation(event.getDocumentation())
                ));
                return node;
            }
            case 1:
                return Match.ofNode(EventDefinition.class, BaseStartEvent.class)
                        .when(SignalEventDefinition.class, e -> {
                            Node<View<StartSignalEvent>, Edge> node = factoryManager.newNode(nodeId, StartSignalEvent.class);

                            StartSignalEvent definition = node.getContent().getDefinition();

                            definition.setGeneral(new BPMNGeneralSet(
                                    new Name(event.getName()),
                                    Properties.documentation(event.getDocumentation())
                            ));

                            definition.setExecutionSet(new InterruptingSignalEventExecutionSet(
                                    new IsInterrupting(event.isIsInterrupting()),
                                    new SignalRef(definitionResolver.resolveSignalName(e.getSignalRef()))
                            ));

                            return node;
                        })
                        .when(MessageEventDefinition.class, e -> {
                            Node<View<StartMessageEvent>, Edge> node = factoryManager.newNode(nodeId, StartMessageEvent.class);

                            StartMessageEvent definition = node.getContent().getDefinition();

                            definition.setGeneral(new BPMNGeneralSet(
                                    new Name(event.getName()),
                                    Properties.documentation(event.getDocumentation())
                            ));

                            definition.getDataIOSet().getAssignmentsinfo().setValue(Properties.getAssignmentsInfo(event));

                            definition.setExecutionSet(new InterruptingMessageEventExecutionSet(
                                    new IsInterrupting(event.isIsInterrupting()),
                                    new MessageRef(e.getMessageRef().getName())
                            ));

                            return node;
                        })
                        .when(TimerEventDefinition.class, e -> {
                            Node<View<StartTimerEvent>, Edge> node = factoryManager.newNode(nodeId, StartTimerEvent.class);

                            StartTimerEvent definition = node.getContent().getDefinition();

                            definition.setGeneral(new BPMNGeneralSet(
                                    new Name(event.getName()),
                                    Properties.documentation(event.getDocumentation())
                            ));

                            definition.setExecutionSet(new InterruptingTimerEventExecutionSet(
                                    new IsInterrupting(event.isIsInterrupting()),
                                    TimerEventDefinitionConverter.convertTimerEventDefinition(e)
                            ));

                            return node;
                        })
                        .when(ErrorEventDefinition.class, e -> {
                            Node<View<StartErrorEvent>, Edge> node = factoryManager.newNode(nodeId, StartErrorEvent.class);

                            StartErrorEvent definition = node.getContent().getDefinition();

                            definition.setGeneral(new BPMNGeneralSet(
                                    new Name(event.getName()),
                                    Properties.documentation(event.getDocumentation())
                            ));

                            definition.getDataIOSet().getAssignmentsinfo().setValue(Properties.getAssignmentsInfo(event));

                            definition.setExecutionSet(new InterruptingErrorEventExecutionSet(
                                    new IsInterrupting(event.isIsInterrupting()),
                                    new ErrorRef(e.getErrorRef().getErrorCode())
                            ));

                            return node;
                        })
                        .missing(ConditionalEventDefinition.class)
                        .missing(EscalationEventDefinition.class)
                        .missing(CompensateEventDefinition.class)
                        .apply(eventDefinitions.get(0)).asSuccess().value();
            default:
                throw new UnsupportedOperationException("Multiple event definitions not supported for start event");
        }
    }
}
