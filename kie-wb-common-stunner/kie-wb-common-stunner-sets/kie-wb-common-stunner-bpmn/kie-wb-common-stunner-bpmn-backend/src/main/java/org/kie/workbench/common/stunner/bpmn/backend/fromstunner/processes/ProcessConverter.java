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
import java.util.stream.Stream;

import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.SubProcess;
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeMatch;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.DefinitionsBuildingContext;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.ElementContainer;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.SequenceFlowConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.ViewDefinitionConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.lanes.LaneConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.ActivityPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.BasePropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.BoundaryEventPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.LanePropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.ProcessPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.SubProcessPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagramImpl;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.bpmn.definition.BaseSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.EmbeddedSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.EventSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.DiagramSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessData;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.definition.Definition;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class ProcessConverter {

    private final DefinitionsBuildingContext context;

    private final ViewDefinitionConverter viewDefinitionConverter;
    private final LaneConverter laneConverter;

    private final SequenceFlowConverter sequenceFlowConverter;

    public ProcessConverter(DefinitionsBuildingContext context) {
        this.context = context;

        this.viewDefinitionConverter =
                new ViewDefinitionConverter(
                        context,
                        this);

        this.laneConverter =
                new LaneConverter();

        this.sequenceFlowConverter =
                new SequenceFlowConverter();

    }

    public ProcessPropertyWriter convertProcess(Node<Definition<BPMNDiagramImpl>, ?> node) {
        ProcessPropertyWriter processRoot = convertProcessNode(node);

        convertChildNodes(processRoot, context.nodes(), context.lanes());
        convertEdges(processRoot, context);

        return processRoot;
    }

    public PropertyWriter convertSubProcess(Node<View<BaseSubprocess>, ?> node) {
        SubProcessPropertyWriter processRoot =
                NodeMatch.fromNode(BaseSubprocess.class, SubProcessPropertyWriter.class)
                        .when(EmbeddedSubprocess.class, this::convertEmbeddedSubprocessNode)
                        .when(EventSubprocess.class, this::convertEventSubprocessNode)
                        .apply(node).value();

        DefinitionsBuildingContext subContext = context.withRootNode(node);

        convertChildNodes(processRoot, subContext.nodes(), subContext.lanes());
        convertEdges(processRoot, subContext);

        return processRoot;
    }

    private ProcessPropertyWriter convertProcessNode(Node<Definition<BPMNDiagramImpl>, ?> node) {
        Process process = bpmn2.createProcess();

        ProcessPropertyWriter p = new ProcessPropertyWriter(process);
        BPMNDiagramImpl definition = node.getContent().getDefinition();

        DiagramSet diagramSet = definition.getDiagramSet();

        p.setName(diagramSet.getName().getValue());
        p.setDocumentation(diagramSet.getDocumentation().getValue());

        process.setId(diagramSet.getId().getValue());
        p.setPackage(diagramSet.getPackageProperty().getValue());
        p.setVersion(diagramSet.getVersion().getValue());
        p.setAdHoc(diagramSet.getAdHoc().getValue());
        p.setDescription(diagramSet.getProcessInstanceDescription().getValue());
        p.setExecutable(diagramSet.getExecutable().getValue());

        ProcessData processData = definition.getProcessData();
        p.setProcessVariables(processData.getProcessVariables());

        return p;
    }

    private SubProcessPropertyWriter convertEventSubprocessNode(Node<View<EventSubprocess>, ?> n) {
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

        return p;
    }

    private SubProcessPropertyWriter convertEmbeddedSubprocessNode(Node<View<EmbeddedSubprocess>, ?> n) {
        SubProcess process = bpmn2.createSubProcess();
        process.setId(n.getUUID());

        SubProcessPropertyWriter p = new SubProcessPropertyWriter(process);

        EmbeddedSubprocess definition = n.getContent().getDefinition();

        BPMNGeneralSet general = definition.getGeneral();

        p.setName(general.getName().getValue());
        p.setDocumentation(general.getDocumentation().getValue());

        ProcessData processData = definition.getProcessData();
        p.setProcessVariables(processData.getProcessVariables());

        p.setSimulationSet(definition.getSimulationSet());
        p.setBounds(n.getContent().getBounds());
        return p;
    }

    private void convertChildNodes(
            ElementContainer p,
            Stream<? extends Node<View<? extends BPMNViewDefinition>, ?>> nodes,
            Stream<? extends Node<View<? extends BPMNViewDefinition>, ?>> lanes) {
        nodes.map(viewDefinitionConverter::toFlowElement)
                .filter(Result::notIgnored)
                .map(Result::value)
                .forEach(p::addChildElement);

        convertLanes(lanes, p);
    }

    private void convertLanes(
            Stream<? extends Node<View<? extends BPMNViewDefinition>, ?>> lanes,
            ElementContainer p) {
        List<LanePropertyWriter> collect = lanes
                .map(laneConverter::toElement)
                .filter(Result::notIgnored)
                .map(Result::value)
                .collect(Collectors.toList());

        p.addLaneSet(collect);
        collect.forEach(p::addChildElement);
    }

    private void convertEdges(ElementContainer p, DefinitionsBuildingContext context) {
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