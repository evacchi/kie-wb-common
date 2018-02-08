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

package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.tasks;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Task;
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeMatch;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.FlowElementPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.BaseTask;
import org.kie.workbench.common.stunner.bpmn.definition.NoneTask;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public class TaskConverter {

    private final Bpmn2Factory bpmn2 = Bpmn2Factory.eINSTANCE;

    public FlowNode toFlowElement(Node<View<BaseTask>, ?> node) {
        return NodeMatch.fromNode(BaseTask.class, FlowNode.class)
                .when(NoneTask.class, n -> {
                    Task task = bpmn2.createTask();
                    NoneTask definition = n.getContent().getDefinition();
                    FlowElementPropertyWriter p = new FlowElementPropertyWriter(task);
                    task.setId(n.getUUID());
                    p.setName(definition.getGeneral().getName().getValue());
                    return task;
                }).apply(node).value();
    }
}
