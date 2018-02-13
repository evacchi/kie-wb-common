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

import org.eclipse.bpmn2.Task;
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeMatch;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.Scripts;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.ActivityPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.ScriptTaskPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.BaseTask;
import org.kie.workbench.common.stunner.bpmn.definition.BusinessRuleTask;
import org.kie.workbench.common.stunner.bpmn.definition.NoneTask;
import org.kie.workbench.common.stunner.bpmn.definition.ScriptTask;
import org.kie.workbench.common.stunner.bpmn.definition.UserTask;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.TaskGeneralSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.BusinessRuleTaskExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ScriptTaskExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ScriptTypeValue;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.UserTaskExecutionSet;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class TaskConverter {

    public PropertyWriter toFlowElement(Node<View<BaseTask>, ?> node) {
        return NodeMatch.fromNode(BaseTask.class, PropertyWriter.class)
                .when(NoneTask.class, n -> {
                    Task task = bpmn2.createTask();
                    NoneTask definition = n.getContent().getDefinition();
                    PropertyWriter p = new PropertyWriter(task);
                    task.setId(n.getUUID());
                    p.setName(definition.getGeneral().getName().getValue());
                    p.setBounds(n.getContent().getBounds());
                    return p;
                })
                .when(ScriptTask.class, n -> {
                    org.eclipse.bpmn2.ScriptTask task = bpmn2.createScriptTask();
                    ScriptTask definition = n.getContent().getDefinition();
                    ScriptTaskPropertyWriter p = new ScriptTaskPropertyWriter(task);

                    task.setId(n.getUUID());

                    TaskGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    ScriptTaskExecutionSet executionSet = definition.getExecutionSet();

                    p.setScript(executionSet.getScript().getValue());
                    p.setAsync(executionSet.getIsAsync().getValue());

                    p.setBounds(n.getContent().getBounds());
                    return p;
                })
                .when(BusinessRuleTask.class, n -> {
                    org.eclipse.bpmn2.ScriptTask task = bpmn2.createScriptTask();
                    BusinessRuleTask definition = n.getContent().getDefinition();
                    ScriptTaskPropertyWriter p = new ScriptTaskPropertyWriter(task);

                    task.setId(n.getUUID());

                    TaskGeneralSet general = definition.getGeneral();
                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    BusinessRuleTaskExecutionSet executionSet = definition.getExecutionSet();

                    // p.setOnEntryScript(executionSet.getOnEntryAction());
                    // p.setOnExitScript(executionSet.getOnExitAction());

                    p.setAsync(executionSet.getIsAsync().getValue());

                    p.setBounds(n.getContent().getBounds());
                    return p;
                })
                .when(UserTask.class, n -> {
                    Task task = bpmn2.createTask();
                    UserTask definition = n.getContent().getDefinition();
                    ActivityPropertyWriter p = new ActivityPropertyWriter(task);
                    task.setId(n.getUUID());
                    p.setName(definition.getGeneral().getName().getValue());
                    p.setBounds(n.getContent().getBounds());

                    UserTaskExecutionSet executionSet = definition.getExecutionSet();
                    p.setAssignmentsInfo(executionSet.getAssignmentsinfo());

                    return p;
                }).apply(node).value();
    }

    protected String asCData(String value) {
        return "<![CDATA[" + value + "]]>";
    }
}
