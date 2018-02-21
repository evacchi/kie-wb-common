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

package org.kie.workbench.common.stunner.bpmn.backend.converters.processes;

import java.util.List;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.LaneSet;
import org.eclipse.bpmn2.Process;
import org.kie.workbench.common.stunner.bpmn.backend.converters.GraphBuildingContext;
import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeResult;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.ProcessPropertyReader;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.PropertyReaderFactory;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagramImpl;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.AdHoc;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.DiagramSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.Executable;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.Id;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.Package;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.ProcessInstanceDescription;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.Version;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.Documentation;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.Name;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessData;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessVariables;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public class ProcessConverter extends AbstractProcessConverter {

    public ProcessConverter(TypedFactoryManager typedFactoryManager, PropertyReaderFactory propertyReaderFactory, GraphBuildingContext context) {
        super(typedFactoryManager, propertyReaderFactory, context);
    }

    public NodeResult<BPMNDiagramImpl> convert(String id, Process process) {
        Node<View<BPMNDiagramImpl>, Edge> diagramNode =
                factoryManager.newNode(id, BPMNDiagramImpl.class);
        BPMNDiagramImpl definition = diagramNode.getContent().getDefinition();

        ProcessPropertyReader e = propertyReaderFactory.of(process);

        definition.setDiagramSet(new DiagramSet(
                new Name(process.getName()),
                new Documentation(e.getDocumentation()),
                new Id(process.getId()),
                new Package(e.getPackageName()),
                new Version(e.getVersion()),
                new AdHoc(e.isAdHoc()),
                new ProcessInstanceDescription(e.getDescription()),
                new Executable(process.isIsExecutable())
        ));

        definition.setProcessData(new ProcessData(
                new ProcessVariables(e.getProcessVariables())
        ));

        diagramNode.getContent().setBounds(e.getBounds());

        definition.setFontSet(e.getFontSet());
        definition.setBackgroundSet(e.getBackgroundSet());

        NodeResult<BPMNDiagramImpl> firstNode = NodeResult.of(diagramNode);

        List<FlowElement> flowElements = process.getFlowElements();
        List<LaneSet> laneSets = process.getLaneSets();

        convertNodes(firstNode, flowElements, laneSets);

        return firstNode;
    }

}