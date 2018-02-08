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

package org.kie.workbench.common.stunner.bpmn.backend.fromstunner;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.SequenceFlow;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;

public class SequenceFlowUnconverter {

    private final Bpmn2Factory bpmn2 = Bpmn2Factory.eINSTANCE;
    private final DefinitionsBuildingContextHelper context;

    public SequenceFlowUnconverter(DefinitionsBuildingContextHelper context) {
        this.context = context;
    }

    public SequenceFlow toFlowElement(Edge<ViewConnector<BPMNViewDefinition>, ?> edge) {
        SequenceFlow seq = bpmn2.createSequenceFlow();
        seq.setSourceRef(context.getFlowNode(edge.getSourceNode().getUUID()));
        seq.setTargetRef(context.getFlowNode(edge.getTargetNode().getUUID()));
        seq.setId(edge.getUUID());
        seq.setName(edge.getContent().getDefinition().getGeneral().getName().getValue());
        return seq;
    }


}
