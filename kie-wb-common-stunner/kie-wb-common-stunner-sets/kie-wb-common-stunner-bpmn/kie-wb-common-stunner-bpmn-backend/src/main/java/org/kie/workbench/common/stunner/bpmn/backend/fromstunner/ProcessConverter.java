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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bpsim.BPSimDataType;
import bpsim.BpsimPackage;
import bpsim.ControlParameters;
import bpsim.ElementParameters;
import bpsim.PriorityParameters;
import bpsim.ResourceParameters;
import bpsim.Scenario;
import bpsim.ScenarioParameters;
import bpsim.TimeParameters;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.Relationship;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.dd.di.DiagramElement;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.ProcessPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
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

    public static final String defaultRelationshipType = "BPSimData";

    private final DefinitionsBuildingContext context;

    private final SequenceFlowConverter sequenceFlowConverter;
    private final ViewDefinitionConverter viewDefinitionConverter;
    Map<String, PropertyWriter> props = new HashMap<>();

    public ProcessConverter(DefinitionsBuildingContext context) {
        this.context = context;
        this.sequenceFlowConverter = new SequenceFlowConverter(context);
        this.viewDefinitionConverter = new ViewDefinitionConverter(context);
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

        context.edges()
                .map(e -> sequenceFlowConverter.toFlowElement(e, props))
                .forEach(e -> {
                    p.addFlowElement(e);
                    context.addSequenceFlow(e); // used in shape/edges fixme: drop this
                });

        return p;
    }

    public Relationship toRelationship() {
        Relationship relationship = bpmn2.createRelationship();
        relationship.setType(defaultRelationshipType);
        BPSimDataType simDataType = bpsim.createBPSimDataType();
        // currently support single scenario
        Scenario defaultScenario = bpsim.createScenario();
        ScenarioParameters scenarioParameters = bpsim.createScenarioParameters();
        defaultScenario.setId("default"); // single scenario suppoert
        defaultScenario.setName("Simulationscenario"); // single scenario support
        defaultScenario.setScenarioParameters(scenarioParameters);
        simDataType.getScenario().add(defaultScenario);
        ExtensionAttributeValue extensionElement = bpmn2.createExtensionAttributeValue();
        relationship.getExtensionValues().add(extensionElement);
        FeatureMap.Entry extensionElementEntry = new EStructuralFeatureImpl.SimpleFeatureMapEntry(
                (EStructuralFeature.Internal) BpsimPackage.Literals.DOCUMENT_ROOT__BP_SIM_DATA,
                simDataType);
        relationship.getExtensionValues().get(0).getValue().add(extensionElementEntry);

        ElementParameters elementParameters = bpsim.createElementParameters();

        ControlParameters controlParameters = bpsim.createControlParameters();
        PriorityParameters priorityParameters = bpsim.createPriorityParameters();
        ResourceParameters resourceParameters = bpsim.createResourceParameters();
        TimeParameters timeParameters = bpsim.createTimeParameters();

        elementParameters.setControlParameters(controlParameters);
        elementParameters.setPriorityParameters(priorityParameters);
        elementParameters.setResourceParameters(resourceParameters);
        elementParameters.setTimeParameters(timeParameters);

        defaultScenario.getElementParameters().add(elementParameters);

        return relationship;
    }

    public org.eclipse.bpmn2.di.BPMNDiagram toBPMNDiagram() {
        org.eclipse.bpmn2.di.BPMNDiagram bpmnDiagram = di.createBPMNDiagram();
        bpmnDiagram.setId(context.firstNode().getUUID());

        BPMNPlane bpmnPlane = di.createBPMNPlane();
        bpmnDiagram.setPlane(bpmnPlane);

        List<DiagramElement> planeElement =
                bpmnPlane.getPlaneElement();

        props.values().forEach(p -> planeElement.add(p.getShape()));

        context.edges()
                .map(e -> viewDefinitionConverter.edgeFrom(props, e))
                .forEach(planeElement::add);

        return bpmnDiagram;
    }
}