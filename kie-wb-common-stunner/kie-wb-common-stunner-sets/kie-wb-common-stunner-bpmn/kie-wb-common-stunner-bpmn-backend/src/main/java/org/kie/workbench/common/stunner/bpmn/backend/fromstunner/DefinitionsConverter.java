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

import org.eclipse.bpmn2.Definitions;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.processes.ProcessConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.processes.ProcessConverterFactory;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.DefinitionsPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.ProcessPropertyWriter;
import org.kie.workbench.common.stunner.core.graph.Graph;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class DefinitionsConverter {

    private final DefinitionsBuildingContext context;
    private final ProcessConverter processConverter;

    public DefinitionsConverter(DefinitionsBuildingContext context) {
        this.context = context;
        this.processConverter = new ProcessConverter(context, new ProcessConverterFactory(context));
    }

    public DefinitionsConverter(Graph graph) {
        this(new DefinitionsBuildingContext(graph));
    }

    public Definitions toDefinitions() {
        Definitions definitions = bpmn2.createDefinitions();
        DefinitionsPropertyWriter p =
                new DefinitionsPropertyWriter(definitions);

        ProcessPropertyWriter pp =
                processConverter.convertProcess();

        p.setProcess(pp.getProcess());
        p.setDiagram(pp.getBpmnDiagram());
        p.setRelationship(pp.getRelationship());
        p.addAllRootElements(pp.getRootElements());

        return definitions;
    }
}
