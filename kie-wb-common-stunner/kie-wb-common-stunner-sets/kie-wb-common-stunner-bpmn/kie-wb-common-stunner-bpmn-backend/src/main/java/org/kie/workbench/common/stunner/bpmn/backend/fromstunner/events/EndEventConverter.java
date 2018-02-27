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
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeMatch;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriterFactory;
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

    private final PropertyWriterFactory propertyWriterFactory;

    public EndEventConverter(PropertyWriterFactory propertyWriterFactory) {
        this.propertyWriterFactory = propertyWriterFactory;
    }

    public PropertyWriter toFlowElement(Node<View<BaseEndEvent>, ?> node) {
        return NodeMatch.fromNode(BaseEndEvent.class, PropertyWriter.class)
                .when(EndNoneEvent.class, this::noneEvent)
                .when(EndMessageEvent.class, this::messageEvent)
                .when(EndSignalEvent.class, this::signalEvent)
                .when(EndTerminateEvent.class, this::terminateEvent)
                .when(EndErrorEvent.class, this::errorEvent)
                .apply(node).value();
    }

    private PropertyWriter errorEvent(Node<View<EndErrorEvent>, ?> n) {
        EndEvent event = bpmn2.createEndEvent();
        event.setId(n.getUUID());

        EndErrorEvent definition = n.getContent().getDefinition();
        ThrowEventPropertyWriter p = propertyWriterFactory.of(event);

        BPMNGeneralSet general = definition.getGeneral();
        p.setName(general.getName().getValue());
        p.setDocumentation(general.getDocumentation().getValue());

        p.setAssignmentsInfo(
                definition.getDataIOSet().getAssignmentsinfo());

        ErrorEventExecutionSet executionSet = definition.getExecutionSet();
        p.addError(executionSet.getErrorRef());

        p.setBounds(n.getContent().getBounds());
        return p;
    }

    private PropertyWriter terminateEvent(Node<View<EndTerminateEvent>, ?> n) {
        EndEvent event = bpmn2.createEndEvent();
        event.setId(n.getUUID());

        EndTerminateEvent definition = n.getContent().getDefinition();
        ThrowEventPropertyWriter p = propertyWriterFactory.of(event);

        BPMNGeneralSet general = definition.getGeneral();
        p.setName(general.getName().getValue());
        p.setDocumentation(general.getDocumentation().getValue());

        p.addTerminate();

        p.setBounds(n.getContent().getBounds());
        return p;
    }

    private PropertyWriter signalEvent(Node<View<EndSignalEvent>, ?> n) {
        EndEvent event = bpmn2.createEndEvent();
        event.setId(n.getUUID());

        EndSignalEvent definition = n.getContent().getDefinition();
        ThrowEventPropertyWriter p = propertyWriterFactory.of(event);

        BPMNGeneralSet general = definition.getGeneral();
        p.setName(general.getName().getValue());
        p.setDocumentation(general.getDocumentation().getValue());

        p.setAssignmentsInfo(
                definition.getDataIOSet().getAssignmentsinfo());

        p.addSignal(definition.getExecutionSet().getSignalRef());

        p.setBounds(n.getContent().getBounds());
        return p;
    }

    private PropertyWriter messageEvent(Node<View<EndMessageEvent>, ?> n) {
        EndEvent event = bpmn2.createEndEvent();
        event.setId(n.getUUID());

        EndMessageEvent definition = n.getContent().getDefinition();
        ThrowEventPropertyWriter p = propertyWriterFactory.of(event);

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
    }

    private PropertyWriter noneEvent(Node<View<EndNoneEvent>, ?> n) {
        EndEvent event = bpmn2.createEndEvent();
        event.setId(n.getUUID());

        BaseEndEvent definition = n.getContent().getDefinition();
        ThrowEventPropertyWriter p = propertyWriterFactory.of(event);

        BPMNGeneralSet general = definition.getGeneral();
        p.setName(general.getName().getValue());
        p.setDocumentation(general.getDocumentation().getValue());

        p.setBounds(n.getContent().getBounds());
        return p;
    }
}
