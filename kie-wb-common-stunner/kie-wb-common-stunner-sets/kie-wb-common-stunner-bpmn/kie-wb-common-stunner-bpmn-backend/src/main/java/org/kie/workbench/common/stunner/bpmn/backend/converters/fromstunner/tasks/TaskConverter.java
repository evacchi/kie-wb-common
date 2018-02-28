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

package org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.tasks;

import org.eclipse.bpmn2.Task;
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeMatch;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.ActivityPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.BusinessRuleTaskPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.PropertyWriterFactory;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.ScriptTaskPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.UserTaskPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.BaseTask;
import org.kie.workbench.common.stunner.bpmn.definition.BusinessRuleTask;
import org.kie.workbench.common.stunner.bpmn.definition.NoneTask;
import org.kie.workbench.common.stunner.bpmn.definition.ScriptTask;
import org.kie.workbench.common.stunner.bpmn.definition.UserTask;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.TaskGeneralSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.BusinessRuleTaskExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ScriptTaskExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.UserTaskExecutionSet;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

import static org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.Factories.bpmn2;

public class TaskConverter {

    private final PropertyWriterFactory propertyWriterFactory;

    public TaskConverter(PropertyWriterFactory propertyWriterFactory) {
        this.propertyWriterFactory = propertyWriterFactory;
    }

    public PropertyWriter toFlowElement(Node<View<BaseTask>, ?> node) {
        return NodeMatch.fromNode(BaseTask.class, PropertyWriter.class)
                .when(NoneTask.class, this::noneTask)
                .when(ScriptTask.class, this::scriptTask)
                .when(BusinessRuleTask.class, this::businessRuleTask)
                .when(UserTask.class, this::userTask)
                .apply(node).value();
    }

    private PropertyWriter userTask(Node<View<UserTask>, ?> n) {
        org.eclipse.bpmn2.UserTask task = bpmn2.createUserTask();
        task.setId(n.getUUID());
        UserTask definition = n.getContent().getDefinition();
        UserTaskPropertyWriter p = propertyWriterFactory.of(task);

        TaskGeneralSet general = definition.getGeneral();
        p.setName(general.getName().getValue());
        p.setDocumentation(general.getDocumentation().getValue());

        p.setSimulationSet(definition.getSimulationSet());

        UserTaskExecutionSet executionSet = definition.getExecutionSet();

        p.setTaskName(executionSet.getTaskName().getValue());
        p.setActors(executionSet.getActors());
        p.setGroupId(executionSet.getGroupid().getValue());
        p.setAssignmentsInfo(executionSet.getAssignmentsinfo());
        p.setAsync(executionSet.getIsAsync().getValue());
        p.setSkippable(executionSet.getSkippable().getValue());
        p.setPriority(executionSet.getPriority().getValue());
        p.setSubject(executionSet.getSubject().getValue());
        p.setDescription(executionSet.getDescription().getValue());
        p.setAdHocAutostart(executionSet.getAdHocAutostart().getValue());
        p.setCreatedBy(executionSet.getCreatedBy().getValue());
        p.setOnEntryAction(executionSet.getOnEntryAction());
        p.setOnExitAction(executionSet.getOnExitAction());

        p.setBounds(n.getContent().getBounds());

        return p;
    }

    private PropertyWriter businessRuleTask(Node<View<BusinessRuleTask>, ?> n) {
        org.eclipse.bpmn2.BusinessRuleTask task = bpmn2.createBusinessRuleTask();
        task.setId(n.getUUID());
        BusinessRuleTask definition = n.getContent().getDefinition();
        BusinessRuleTaskPropertyWriter p = propertyWriterFactory.of(task);

        TaskGeneralSet general = definition.getGeneral();
        p.setName(general.getName().getValue());
        p.setDocumentation(general.getDocumentation().getValue());

        BusinessRuleTaskExecutionSet executionSet =
                definition.getExecutionSet();

        p.setAsync(executionSet.getIsAsync().getValue());
        p.setOnEntryAction(executionSet.getOnEntryAction());
        p.setOnExitAction(executionSet.getOnExitAction());
        p.setRuleFlowGroup(executionSet.getRuleFlowGroup());
        p.setAdHocAutostart(executionSet.getAdHocAutostart().getValue());

        p.setAssignmentsInfo(definition.getDataIOSet().getAssignmentsinfo());

        p.setSimulationSet(definition.getSimulationSet());

        p.setBounds(n.getContent().getBounds());
        return p;
    }

    private PropertyWriter scriptTask(Node<View<ScriptTask>, ?> n) {
        org.eclipse.bpmn2.ScriptTask task = bpmn2.createScriptTask();
        task.setId(n.getUUID());
        ScriptTask definition = n.getContent().getDefinition();
        ScriptTaskPropertyWriter p = propertyWriterFactory.of(task);

        TaskGeneralSet general = definition.getGeneral();
        p.setName(general.getName().getValue());
        p.setDocumentation(general.getDocumentation().getValue());

        ScriptTaskExecutionSet executionSet = definition.getExecutionSet();

        p.setScript(executionSet.getScript().getValue());
        p.setAsync(executionSet.getIsAsync().getValue());

        p.setSimulationSet(definition.getSimulationSet());

        p.setBounds(n.getContent().getBounds());
        return p;
    }

    private PropertyWriter noneTask(Node<View<NoneTask>, ?> n) {
        Task task = bpmn2.createTask();
        task.setId(n.getUUID());
        NoneTask definition = n.getContent().getDefinition();
        ActivityPropertyWriter p =  propertyWriterFactory.of(task);
        p.setName(definition.getGeneral().getName().getValue());
        p.setBounds(n.getContent().getBounds());

        p.setSimulationSet(definition.getSimulationSet());
        return p;
    }

    protected String asCData(String value) {
        return "<![CDATA[" + value + "]]>";
    }
}