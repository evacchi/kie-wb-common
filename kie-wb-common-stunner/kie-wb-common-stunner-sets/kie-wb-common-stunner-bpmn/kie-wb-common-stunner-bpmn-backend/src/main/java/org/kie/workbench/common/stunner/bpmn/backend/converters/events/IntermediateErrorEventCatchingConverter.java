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

import org.eclipse.bpmn2.ErrorEventDefinition;
import org.eclipse.bpmn2.IntermediateCatchEvent;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.Properties;
import org.kie.workbench.common.stunner.bpmn.definition.IntermediateErrorEventCatching;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.CancelActivity;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.error.CancellingErrorEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.error.ErrorRef;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.Name;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public class IntermediateErrorEventCatchingConverter {

    private final TypedFactoryManager factoryManager;

    public IntermediateErrorEventCatchingConverter(TypedFactoryManager factoryManager) {
        this.factoryManager = factoryManager;
    }

    public Node<View<IntermediateErrorEventCatching>, Edge> convert(IntermediateCatchEvent event, ErrorEventDefinition e) {
        String nodeId = event.getId();
        Node<View<IntermediateErrorEventCatching>, Edge> node = factoryManager.newNode(nodeId, IntermediateErrorEventCatching.class);

        IntermediateErrorEventCatching definition = node.getContent().getDefinition();

        definition.setGeneral(new BPMNGeneralSet(
                new Name(event.getName()),
                Properties.documentation(event.getDocumentation())
        ));

        definition.getDataIOSet().getAssignmentsinfo().setValue(Properties.getAssignmentsInfo(event));

        definition.setExecutionSet(new CancellingErrorEventExecutionSet(
                new CancelActivity(true),
                new ErrorRef(e.getErrorRef().getErrorCode())
        ));

        return node;
    }
}
