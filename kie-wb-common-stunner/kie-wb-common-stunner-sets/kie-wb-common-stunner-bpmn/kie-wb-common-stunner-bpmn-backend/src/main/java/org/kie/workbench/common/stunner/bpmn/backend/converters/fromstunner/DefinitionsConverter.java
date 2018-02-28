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

package org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner;

import org.eclipse.bpmn2.Definitions;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.processes.ProcessConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.processes.ProcessConverterFactory;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.DefinitionsPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.ProcessPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.PropertyWriterFactory;
import org.kie.workbench.common.stunner.core.graph.Graph;

import static org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.Factories.bpmn2;

public class DefinitionsConverter {

    private final DefinitionsBuildingContext context;
    private final ProcessConverter processConverter;
    private final PropertyWriterFactory propertyWriterFactory;

    public DefinitionsConverter(DefinitionsBuildingContext context, PropertyWriterFactory propertyWriterFactory) {
        this.context = context;
        this.processConverter = new ProcessConverter(context, propertyWriterFactory, new ProcessConverterFactory(context, propertyWriterFactory));
        this.propertyWriterFactory = propertyWriterFactory;
    }

    public DefinitionsConverter(Graph graph) {
        this(new DefinitionsBuildingContext(graph), new PropertyWriterFactory());
    }

    public Definitions toDefinitions() {
        Definitions definitions = bpmn2.createDefinitions();
        DefinitionsPropertyWriter p = propertyWriterFactory.of(definitions);

        ProcessPropertyWriter pp =
                processConverter.convertProcess();

        p.setProcess(pp.getProcess());
        p.setDiagram(pp.getBpmnDiagram());
        p.setRelationship(pp.getRelationship());
        p.addAllRootElements(pp.getRootElements());
        p.addAllRootElements(pp.getItemDefinitions());

        return definitions;
    }
}
