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

package org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties;

import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.kie.workbench.common.stunner.bpmn.backend.converters.customproperties.CustomInput;
import org.kie.workbench.common.stunner.bpmn.backend.converters.customproperties.CustomInputDefinition;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.DefinitionResolver;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.TypedAssignmentsInfo;
import org.kie.workbench.common.stunner.bpmn.workitem.WorkItemDefinition;

public class TypedServiceTaskPropertyReader extends ServiceTaskPropertyReader {

    private final WorkItemDefinition workItemDefinition;
    final CustomInput<Object> typedParameters;
    final CustomInput<Object> typedResults;

    public TypedServiceTaskPropertyReader(
            Task task,
            WorkItemDefinition workItemDefinition,
            BPMNPlane plane,
            DefinitionResolver definitionResolver) {
        super(task, workItemDefinition, plane, definitionResolver);
        this.workItemDefinition = workItemDefinition;
        this.typedParameters = null;
        this.typedResults = null;
    }

    public AssignmentsInfo getTypedAssignmentsInfo() {
        AssignmentsInfo assignmentsInfo = super.getAssignmentsInfo();

        return new TypedAssignmentsInfo(
                assignmentsInfo.getValue(),
                workItemDefinition.getTypedParameters(),
                workItemDefinition.getTypedResults());
    }

    static class JsonDefinition extends CustomInputDefinition<Object> {

        private final ObjectMapper mapper;
        private final Class<?> classDef;

        public JsonDefinition(String type) {
            super("$" + type, type, null);
            try {
                this.classDef = Class.forName(type);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
            this.mapper = new ObjectMapper();
        }

        @Override
        public Object getValue(Task element) {
            Optional<String> stringValue = getStringValue(element);
            if (stringValue.isPresent()) {
                try {
                    return mapper.readValue(stringValue.get(), classDef);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }
    }
}
