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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import bpsim.BPSimDataType;
import bpsim.BpsimPackage;
import bpsim.ControlParameters;
import bpsim.ElementParameters;
import bpsim.PriorityParameters;
import bpsim.ResourceParameters;
import bpsim.Scenario;
import bpsim.ScenarioParameters;
import bpsim.TimeParameters;
import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.BoundaryEvent;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Lane;
import org.eclipse.bpmn2.LaneSet;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.Relationship;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.dd.di.DiagramElement;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.lanes.LaneConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.ActivityPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.BasePropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.BoundaryEventPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.LanePropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.ProcessPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.SequenceFlowPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagram;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagramImpl;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.DeclarationList;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.DiagramSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessData;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.definition.Definition;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;
import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpsim;
import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.di;

public class ProcessConverter {


    private final DefinitionsBuildingContext context;

    private final SequenceFlowConverter sequenceFlowConverter;
    private final ViewDefinitionConverter viewDefinitionConverter;
    private final LaneConverter laneConverter;
    Map<String, BasePropertyWriter> props = new HashMap<>();

    public ProcessConverter(DefinitionsBuildingContext context) {
        this.context = context;
        this.sequenceFlowConverter = new SequenceFlowConverter(context);
        this.viewDefinitionConverter = new ViewDefinitionConverter();
        this.laneConverter = new LaneConverter();
    }

    public ProcessPropertyWriter toFlowElement(Node<Definition<BPMNDiagramImpl>, ?> node) {
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

        p.setSimulationSet(null); // fixme: inserting default data

        context.nodes()
                .map(viewDefinitionConverter::toFlowElement)
                .filter(Result::notIgnored)
                .map(Result::value)
                .forEach(pp -> {
                    props.put(pp.getFlowElement().getId(), pp);
                    p.addFlowElement(pp.getFlowElement());
                    context.addFlowNode(pp.getFlowElement()); // used in seq flow fixme: drop this
                    p.addAllBaseElements(pp.getBaseElements());
                });

        LaneSet laneSet = bpmn2.createLaneSet();

        context.nodes()
                .map(laneConverter::toElement)
                .filter(Result::notIgnored)
                .map(Result::value)
                .forEach(pp -> {
                    laneSet.getLanes().add(pp.getElement());
                    props.put(pp.getElement().getId(), pp);
                });

        if (!laneSet.getLanes().isEmpty()) {
            p.getProcess().getLaneSets().add(laneSet);
        }

        context.childEdges()
                .forEach(e -> {
                    BasePropertyWriter pSrc = props.get(e.getSourceNode().getUUID());
                    // if it's null, then it's a root: skip it
                    if (pSrc != null) {
                        BasePropertyWriter pTgt = props.get(e.getTargetNode().getUUID());
                        pTgt.setParent(pSrc);
                    }
                });

        context.dockEdges()
                .forEach(e -> {
                    ActivityPropertyWriter pSrc =
                            (ActivityPropertyWriter) props.get(e.getSourceNode().getUUID());
                    BoundaryEventPropertyWriter pTgt =
                            (BoundaryEventPropertyWriter) props.get(e.getTargetNode().getUUID());

                    pTgt.setParentActivity(pSrc);
                });


        props.values().forEach(pp -> p.addChildShape(pp.getShape()));
        context.edges()
                .forEach(e -> {
                    SequenceFlowPropertyWriter pp = sequenceFlowConverter.toFlowElement(e, props);
                    p.addFlowElement(pp.getFlowElement());
                    p.addChildEdge(pp.getEdge());
                });

        return p;
    }

}