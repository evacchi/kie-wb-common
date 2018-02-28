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

import org.eclipse.bpmn2.Process;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.DefinitionsBuildingContext;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.ProcessPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriterFactory;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagramImpl;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.DiagramSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessData;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.definition.Definition;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class ProcessConverter {

    private final DefinitionsBuildingContext context;
    private final PropertyWriterFactory propertyWriterFactory;
    private final ProcessConverterFactory processConverterFactory;

    public ProcessConverter(DefinitionsBuildingContext context, PropertyWriterFactory propertyWriterFactory, ProcessConverterFactory processConverterFactory) {
        this.context = context;
        this.propertyWriterFactory = propertyWriterFactory;
        this.processConverterFactory = processConverterFactory;
    }

    public ProcessPropertyWriter convertProcess() {
        ProcessPropertyWriter processRoot = convertProcessNode(context.firstNode());

        processConverterFactory.convertChildNodes(processRoot, context.nodes(), context.lanes());
        processConverterFactory.convertEdges(processRoot, context);

        return processRoot;
    }

    private ProcessPropertyWriter convertProcessNode(Node<Definition<BPMNDiagramImpl>, ?> node) {
        Process process = bpmn2.createProcess();

        ProcessPropertyWriter p = propertyWriterFactory.of(process);
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
}