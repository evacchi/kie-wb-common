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

import org.eclipse.bpmn2.StartEvent;
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeMatch;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.CatchEventPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.BaseStartEvent;
import org.kie.workbench.common.stunner.bpmn.definition.StartErrorEvent;
import org.kie.workbench.common.stunner.bpmn.definition.StartMessageEvent;
import org.kie.workbench.common.stunner.bpmn.definition.StartNoneEvent;
import org.kie.workbench.common.stunner.bpmn.definition.StartSignalEvent;
import org.kie.workbench.common.stunner.bpmn.definition.StartTimerEvent;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.error.InterruptingErrorEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.message.InterruptingMessageEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.signal.InterruptingSignalEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.timer.InterruptingTimerEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class StartEventConverter {

    public PropertyWriter toFlowElement(Node<View<BaseStartEvent>, ?> node) {
        return NodeMatch.fromNode(BaseStartEvent.class, PropertyWriter.class)
                .when(StartNoneEvent.class, n -> {
                    StartEvent event = bpmn2.createStartEvent();
                    event.setId(node.getUUID());

                    StartNoneEvent definition = n.getContent().getDefinition();
                    CatchEventPropertyWriter p = new CatchEventPropertyWriter(event);

                    event.setIsInterrupting(false);

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    p.setSimulationSet(definition.getSimulationSet());

                    p.setBounds(n.getContent().getBounds());
                    return p;
                })
                .when(StartSignalEvent.class, n -> {
                    StartEvent event = bpmn2.createStartEvent();
                    event.setId(node.getUUID());

                    StartSignalEvent definition = n.getContent().getDefinition();
                    CatchEventPropertyWriter p = new CatchEventPropertyWriter(event);

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getName().getValue());

                    p.setAssignmentsInfo(
                            definition.getDataIOSet().getAssignmentsinfo());

                    InterruptingSignalEventExecutionSet executionSet =
                            definition.getExecutionSet();

                    p.addSignal(executionSet.getSignalRef());

                    p.setBounds(n.getContent().getBounds());
                    return p;
                })
                .when(StartTimerEvent.class, n -> {
                    StartEvent event = bpmn2.createStartEvent();
                    event.setId(node.getUUID());

                    StartTimerEvent definition = n.getContent().getDefinition();
                    CatchEventPropertyWriter p = new CatchEventPropertyWriter(event);

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getName().getValue());

                    InterruptingTimerEventExecutionSet executionSet = definition.getExecutionSet();
                    event.setIsInterrupting(executionSet.getIsInterrupting().getValue());

                    p.addTimer(executionSet.getTimerSettings());

                    //p(e).setTimerSettings FIXME
                    //p.setSimulationSet

                    p.setBounds(n.getContent().getBounds());
                    return p;
                })
                .when(StartErrorEvent.class, n -> {
                    StartEvent event = bpmn2.createStartEvent();
                    event.setId(node.getUUID());

                    StartErrorEvent definition = n.getContent().getDefinition();
                    CatchEventPropertyWriter p = new CatchEventPropertyWriter(event);

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    p.setAssignmentsInfo(
                            definition.getDataIOSet().getAssignmentsinfo());

                    InterruptingErrorEventExecutionSet executionSet =
                            definition.getExecutionSet();

                    p.addError(executionSet.getErrorRef());

                    p.setBounds(n.getContent().getBounds());
                    return p;
                })

                .when(StartMessageEvent.class, n -> {
                    StartEvent event = bpmn2.createStartEvent();
                    event.setId(node.getUUID());

                    StartMessageEvent definition = n.getContent().getDefinition();
                    CatchEventPropertyWriter p = new CatchEventPropertyWriter(event);

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    p.setAssignmentsInfo(
                            definition.getDataIOSet().getAssignmentsinfo());

                    InterruptingMessageEventExecutionSet executionSet =
                            definition.getExecutionSet();

                    p.addMessage(executionSet.getMessageRef());

                    p.setBounds(n.getContent().getBounds());
                    return p;
                })
                .apply(node).value();
    }
}
