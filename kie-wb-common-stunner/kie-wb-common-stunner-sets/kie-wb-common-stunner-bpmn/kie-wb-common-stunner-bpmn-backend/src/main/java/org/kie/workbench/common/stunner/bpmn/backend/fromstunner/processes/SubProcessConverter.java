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

package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.processes;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.bpmn2.CallActivity;
import org.eclipse.bpmn2.SubProcess;
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeMatch;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.DefinitionsBuildingContext;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.SequenceFlowConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.ViewDefinitionConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.lanes.LaneConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.ActivityPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.BasePropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.BoundaryEventPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.CallActivityPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.LanePropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.SubProcessPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.BaseSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.EmbeddedSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.EventSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.ReusableSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ReusableSubprocessTaskExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessData;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class SubProcessConverter {

    private final DefinitionsBuildingContext context;

    private final SequenceFlowConverter sequenceFlowConverter;
    private final LaneConverter laneConverter;

    public SubProcessConverter(DefinitionsBuildingContext context) {
        this.context = context;
        this.sequenceFlowConverter = new SequenceFlowConverter();
        this.laneConverter = new LaneConverter();
    }

    public PropertyWriter toFlowElement(Node<View<BaseSubprocess>, ?> node) {
        return NodeMatch.fromNode(BaseSubprocess.class, PropertyWriter.class)
                .when(EmbeddedSubprocess.class, n -> {

                    SubProcess process = bpmn2.createSubProcess();
                    process.setId(n.getUUID());

                    SubProcessPropertyWriter p = new SubProcessPropertyWriter(process);

                    EmbeddedSubprocess definition = n.getContent().getDefinition();

                    BPMNGeneralSet general = definition.getGeneral();

                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    ProcessData processData = definition.getProcessData();
                    p.setProcessVariables(processData.getProcessVariables());

                    p.setSimulationSet(null); // fixme: inserting default data

                    p.setBounds(n.getContent().getBounds());

                    addChildNodes(p, node);
                    return p;
                })
                .when(EventSubprocess.class, n -> {

                    SubProcess process = bpmn2.createSubProcess();
                    process.setId(n.getUUID());

                    SubProcessPropertyWriter p = new SubProcessPropertyWriter(process);

                    EventSubprocess definition = n.getContent().getDefinition();
                    process.setTriggeredByEvent(true);

                    BPMNGeneralSet general = definition.getGeneral();

                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    ProcessData processData = definition.getProcessData();
                    p.setProcessVariables(processData.getProcessVariables());

                    p.setSimulationSet(definition.getSimulationSet());

                    p.setBounds(n.getContent().getBounds());

                    addChildNodes(p, node);
                    return p;
                })

                .when(ReusableSubprocess.class, n -> {
                    CallActivity activity = bpmn2.createCallActivity();
                    activity.setId(n.getUUID());

                    CallActivityPropertyWriter p = new CallActivityPropertyWriter(activity);

                    ReusableSubprocess definition = n.getContent().getDefinition();

                    BPMNGeneralSet general = definition.getGeneral();

                    p.setName(general.getName().getValue());
                    p.setDocumentation(general.getDocumentation().getValue());

                    ReusableSubprocessTaskExecutionSet executionSet = definition.getExecutionSet();
                    p.setCalledElement(executionSet.getCalledElement().getValue());
                    p.setAsync(executionSet.getIsAsync().getValue());
                    p.setIndependent(executionSet.getIndependent().getValue());
                    p.setWaitForCompletion(executionSet.getIndependent().getValue());

                    p.setAssignmentsInfo(definition.getDataIOSet().getAssignmentsinfo());

                    p.setSimulationSet(definition.getSimulationSet());

                    p.setBounds(n.getContent().getBounds());
                    return p;
                }).apply(node).value();
    }

    private void addChildNodes(SubProcessPropertyWriter p, Node<View<BaseSubprocess>, ?> node) {
        DefinitionsBuildingContext subContext = context.withRootNode(node);

        ViewDefinitionConverter viewDefinitionConverter =
                new ViewDefinitionConverter(subContext);

        context.nodes()
                .map(viewDefinitionConverter::toFlowElement)
                .filter(Result::notIgnored)
                .map(Result::value)
                .forEach(p::addChildElement);

        List<LanePropertyWriter> lanes = context.nodes()
                .map(laneConverter::toElement)
                .filter(Result::notIgnored)
                .map(Result::value)
                .collect(Collectors.toList());

        p.addLaneSet(lanes);
        lanes.forEach(p::addChildElement);

        context.childEdges()
                .forEach(e -> {
                    BasePropertyWriter pSrc = p.getChildElement(e.getSourceNode().getUUID());
                    // if it's null, then it's a root: skip it
                    if (pSrc != null) {
                        BasePropertyWriter pTgt = p.getChildElement(e.getTargetNode().getUUID());
                        pTgt.setParent(pSrc);
                    }
                });

        context.dockEdges()
                .forEach(e -> {
                    ActivityPropertyWriter pSrc =
                            (ActivityPropertyWriter) p.getChildElement(e.getSourceNode().getUUID());
                    BoundaryEventPropertyWriter pTgt =
                            (BoundaryEventPropertyWriter) p.getChildElement(e.getTargetNode().getUUID());

                    pTgt.setParentActivity(pSrc);
                });

        context.edges()
                .map(e -> sequenceFlowConverter.toFlowElement(e, p))
                .forEach(p::addChildElement);
    }
}