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

import org.kie.workbench.common.stunner.bpmn.backend.converters.NodeMatch;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.activities.ReusableSubprocessConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.events.EndEventConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.events.IntermediateCatchEventConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.events.IntermediateThrowEventConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.events.StartEventConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.gateways.GatewayConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.processes.ProcessConverterFactory;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.processes.SubProcessConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriterFactory;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.tasks.TaskConverter;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.bpmn.definition.BaseCatchingIntermediateEvent;
import org.kie.workbench.common.stunner.bpmn.definition.BaseEndEvent;
import org.kie.workbench.common.stunner.bpmn.definition.BaseGateway;
import org.kie.workbench.common.stunner.bpmn.definition.BaseStartEvent;
import org.kie.workbench.common.stunner.bpmn.definition.BaseSubprocess;
import org.kie.workbench.common.stunner.bpmn.definition.BaseTask;
import org.kie.workbench.common.stunner.bpmn.definition.BaseThrowingIntermediateEvent;
import org.kie.workbench.common.stunner.bpmn.definition.Lane;
import org.kie.workbench.common.stunner.bpmn.definition.ReusableSubprocess;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public class ViewDefinitionConverter {

    private final StartEventConverter startEventConverter;
    private final TaskConverter taskConverter;
    private final EndEventConverter endEventConverter;
    private final IntermediateCatchEventConverter intermediateCatchEventConverter;
    private final IntermediateThrowEventConverter intermediateThrowEventConverter;
    private final ReusableSubprocessConverter reusableSubprocessConverter;
    private final SubProcessConverter subProcessConverter;
    private final GatewayConverter gatewayConverter;
    private final DefinitionsBuildingContext context;
    private final PropertyWriterFactory propertyWriterFactory;

    public ViewDefinitionConverter(
            DefinitionsBuildingContext context,
            PropertyWriterFactory propertyWriterFactory,
            ProcessConverterFactory processConverterFactory) {
        this.context = context;
        this.propertyWriterFactory = propertyWriterFactory;

        this.startEventConverter = new StartEventConverter(propertyWriterFactory);
        this.endEventConverter = new EndEventConverter(propertyWriterFactory);
        this.intermediateCatchEventConverter = new IntermediateCatchEventConverter(propertyWriterFactory);
        this.intermediateThrowEventConverter = new IntermediateThrowEventConverter(propertyWriterFactory);
        this.gatewayConverter = new GatewayConverter(propertyWriterFactory);
        this.taskConverter = new TaskConverter(propertyWriterFactory);
        this.subProcessConverter = processConverterFactory.subProcessConverter();
        this.reusableSubprocessConverter = new ReusableSubprocessConverter(propertyWriterFactory);
    }

    public Result<PropertyWriter> toFlowElement(Node<View<? extends BPMNViewDefinition>, ?> node) {
        return NodeMatch.fromNode(BPMNViewDefinition.class, PropertyWriter.class)
                .when(BaseStartEvent.class, startEventConverter::toFlowElement)
                .when(BaseCatchingIntermediateEvent.class, intermediateCatchEventConverter::toFlowElement)
                .when(BaseThrowingIntermediateEvent.class, intermediateThrowEventConverter::toFlowElement)
                .when(BaseEndEvent.class, endEventConverter::toFlowElement)
                .when(BaseTask.class, taskConverter::toFlowElement)
                .when(BaseGateway.class, gatewayConverter::toFlowElement)
                .when(ReusableSubprocess.class, reusableSubprocessConverter::toFlowElement)
                .when(BaseSubprocess.class, subProcessConverter::convertSubProcess)
                .ignore(Lane.class)
                .apply(node);
    }
}

