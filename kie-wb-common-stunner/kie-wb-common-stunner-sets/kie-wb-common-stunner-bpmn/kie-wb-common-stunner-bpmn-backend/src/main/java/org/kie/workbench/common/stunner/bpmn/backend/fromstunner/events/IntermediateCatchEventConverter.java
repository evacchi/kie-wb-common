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

import java.util.List;

import org.eclipse.bpmn2.CatchEvent;
import org.eclipse.bpmn2.IntermediateCatchEvent;
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeMatch;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.CatchEventPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.BaseCatchingIntermediateEvent;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateErrorEventCatching;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateMessageEventCatching;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateSignalEventCatching;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateTimerEvent;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.error.CancellingErrorEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.message.CancellingMessageEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.timer.CancellingTimerEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.relationship.Dock;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class IntermediateCatchEventConverter {

    public PropertyWriter toFlowElement(Node<View<BaseCatchingIntermediateEvent>, ?> node) {
        return NodeMatch.fromNode(BaseCatchingIntermediateEvent.class, PropertyWriter.class)
                .when(IntermediateMessageEventCatching.class, n -> {
                    CatchEvent event = createCatchEvent(n);
                    event.setId(node.getUUID());

                    IntermediateMessageEventCatching definition = n.getContent().getDefinition();
                    CatchEventPropertyWriter p = new CatchEventPropertyWriter(event);

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    p.setAssignmentsInfo(
                            definition.getDataIOSet().getAssignmentsinfo());

                    CancellingMessageEventExecutionSet executionSet = definition.getExecutionSet();

                    p.addMessage(executionSet.getMessageRef());

                    p.setBounds(n.getContent().getBounds());
                    return p;
                })
                .when(IntermediateSignalEventCatching.class, n -> {
                    CatchEvent event = createCatchEvent(n);
                    event.setId(node.getUUID());

                    IntermediateSignalEventCatching definition = n.getContent().getDefinition();
                    CatchEventPropertyWriter p = new CatchEventPropertyWriter(event);

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    p.setAssignmentsInfo(
                            definition.getDataIOSet().getAssignmentsinfo());

                    p.addSignal(definition.getExecutionSet().getSignalRef());

                    p.setBounds(n.getContent().getBounds());
                    return p;
                })
                .when(IntermediateErrorEventCatching.class, n -> {
                    CatchEvent event = createCatchEvent(n);
                    event.setId(node.getUUID());

                    IntermediateErrorEventCatching definition = n.getContent().getDefinition();
                    CatchEventPropertyWriter p = new CatchEventPropertyWriter(event);

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    p.setAssignmentsInfo(
                            definition.getDataIOSet().getAssignmentsinfo());

                    CancellingErrorEventExecutionSet executionSet = definition.getExecutionSet();
                    p.addError(executionSet.getErrorRef());

                    p.setBounds(n.getContent().getBounds());
                    return p;
                })
                .when(IntermediateTimerEvent.class, n -> {
                    CatchEvent event = createCatchEvent(n);
                    event.setId(node.getUUID());

                    IntermediateTimerEvent definition = n.getContent().getDefinition();
                    CatchEventPropertyWriter p = new CatchEventPropertyWriter(event);

                    BPMNGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    CancellingTimerEventExecutionSet executionSet = definition.getExecutionSet();
                    p.addTimer(executionSet.getTimerSettings());

                    p.setBounds(n.getContent().getBounds());
                    return p;
                })

                .apply(node).value();
    }

    private CatchEvent createCatchEvent(Node n) {
        return isDocked(n)? bpmn2.createBoundaryEvent() : bpmn2.createIntermediateCatchEvent();
    }

    private boolean isDocked(Node node) {
        return null != getDockSourceNode(node);
    }

    @SuppressWarnings("unchecked")
    private Node<View, Edge> getDockSourceNode(final Node<View, Edge> node) {
        List<Edge> inEdges = node.getInEdges();
        if (null != inEdges && !inEdges.isEmpty()) {
            for (Edge edge : inEdges) {
                if (isDockEdge(edge)) {
                    return edge.getSourceNode();
                }
            }
        }
        return null;
    }

    private boolean isViewEdge(final Edge edge) {
        return edge.getContent() instanceof ViewConnector;
    }

    private boolean isDockEdge(final Edge edge) {
        return edge.getContent() instanceof Dock;
    }
}
