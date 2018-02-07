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

package org.kie.workbench.common.stunner.bpmn.backend.unconverters;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.di.BpmnDiFactory;
import org.eclipse.dd.dc.DcFactory;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Match;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.backend.legacy.util.Utils;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.bpmn.definition.EndNoneEvent;
import org.kie.workbench.common.stunner.bpmn.definition.NoneTask;
import org.kie.workbench.common.stunner.bpmn.definition.StartNoneEvent;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public class FlowElementUnconverter {

    private final Bpmn2Factory bpmn2 = Bpmn2Factory.eINSTANCE;

    public Result<FlowNode> unconvert(Node<View<? extends BPMNViewDefinition>, ?> node) {
        View<? extends BPMNViewDefinition> content = node.getContent();
        BPMNViewDefinition definition = content.getDefinition();
        return Match.of(BPMNViewDefinition.class, FlowNode.class)
                .when(StartNoneEvent.class, event -> {
                    StartEvent startEvent = bpmn2.createStartEvent();
                    FlowElementPropertyWriter p = new FlowElementPropertyWriter(startEvent);
                    startEvent.setId(node.getUUID());
                    startEvent.setIsInterrupting(false);
                    p.setName(event.getGeneral().getName().getValue());
                    return startEvent;
                })
                .when(NoneTask.class, noneTask -> {
                    Task task = bpmn2.createTask();
                    FlowElementPropertyWriter p = new FlowElementPropertyWriter(task);
                    task.setId(node.getUUID());
                    p.setName(noneTask.getGeneral().getName().getValue());
                    return task;
                })
                .when(EndNoneEvent.class, event -> {
                    EndEvent endEvent = bpmn2.createEndEvent();
                    FlowElementPropertyWriter p = new FlowElementPropertyWriter(endEvent);
                    endEvent.setId(node.getUUID());
                    p.setName(event.getGeneral().getName().getValue());
                    return endEvent;
                })
                .apply(definition);
    }
}

class FlowElementPropertyWriter {

    private final FlowElement flowElement;
    private final BpmnDiFactory di = BpmnDiFactory.eINSTANCE;
    private final DcFactory dc = DcFactory.eINSTANCE;

    public FlowElementPropertyWriter(FlowElement flowElement) {
        this.flowElement = flowElement;
    }

    public void setName(String value) {
        flowElement.setName(value);
        setMeta("elementname", value);
    }

    private void setMeta(String attributeId, String value) {
        Utils.setMetaDataExtensionValue(
                flowElement,
                attributeId,
                value);
    }
}

