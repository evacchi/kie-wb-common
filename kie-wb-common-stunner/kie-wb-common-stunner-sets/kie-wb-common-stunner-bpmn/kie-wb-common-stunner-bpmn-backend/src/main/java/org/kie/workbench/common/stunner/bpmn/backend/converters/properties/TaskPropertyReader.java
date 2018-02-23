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

package org.kie.workbench.common.stunner.bpmn.backend.converters.properties;

import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.kie.workbench.common.stunner.bpmn.backend.converters.DefinitionResolver;
import org.kie.workbench.common.stunner.bpmn.definition.property.simulation.SimulationSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ScriptTypeListValue;

public class TaskPropertyReader extends FlowElementPropertyReader {

    protected final Task task;
    protected final DefinitionResolver definitionResolver;

    public TaskPropertyReader(Task task, BPMNPlane plane, DefinitionResolver definitionResolver) {
        super(task, plane, definitionResolver.getShape(task.getId()));
        this.task = task;
        this.definitionResolver = definitionResolver;
    }

    public SimulationSet getSimulationSet() {
        return definitionResolver.resolveSimulationParameters(task.getId())
                .map(Simulations::simulationSet)
                .orElse(new SimulationSet());
    }
}
