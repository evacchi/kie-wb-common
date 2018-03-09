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

package org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner;

import org.eclipse.bpmn2.BoundaryEvent;
import org.eclipse.bpmn2.CallActivity;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.IntermediateCatchEvent;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.SubProcess;
import org.eclipse.bpmn2.Task;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Match;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.activities.CallActivityConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.events.EndEventConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.events.IntermediateCatchEventConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.events.IntermediateThrowEventConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.events.StartEventConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.gateways.GatewayConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.processes.ProcessConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties.PropertyReaderFactory;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.tasks.TaskConverter;

public class FlowElementConverter {

    private final TypedFactoryManager factoryManager;
    private final StartEventConverter startEventConverter;
    private final TaskConverter taskConverter;
    private final GatewayConverter gatewayConverter;
    private final EndEventConverter endEventConverter;
    private final IntermediateThrowEventConverter intermediateThrowEventConverter;
    private final IntermediateCatchEventConverter intermediateCatchEventConverter;
    private final CallActivityConverter callActivityConverter;
    private final ProcessConverter processConverter;

    public FlowElementConverter(
            TypedFactoryManager factoryManager,
            PropertyReaderFactory propertyReaderFactory,
            ProcessConverter processConverter) {

        this.factoryManager = factoryManager;
        this.startEventConverter = new StartEventConverter(factoryManager, propertyReaderFactory);
        this.endEventConverter = new EndEventConverter(factoryManager, propertyReaderFactory);
        this.intermediateThrowEventConverter = new IntermediateThrowEventConverter(factoryManager, propertyReaderFactory);
        this.intermediateCatchEventConverter = new IntermediateCatchEventConverter(factoryManager, propertyReaderFactory);
        this.taskConverter = new TaskConverter(factoryManager, propertyReaderFactory);
        this.gatewayConverter = new GatewayConverter(factoryManager, propertyReaderFactory);
        this.callActivityConverter = new CallActivityConverter(factoryManager, propertyReaderFactory);
        this.processConverter = processConverter;
    }

    public Result<BpmnNode> convertNode(FlowElement flowElement) {
        return Match.of(FlowElement.class, BpmnNode.class)
                .when(StartEvent.class, startEventConverter::convert)
                .when(EndEvent.class, endEventConverter::convert)
                .when(BoundaryEvent.class, intermediateCatchEventConverter::convertBoundaryEvent)
                .when(IntermediateCatchEvent.class, intermediateCatchEventConverter::convertIntermediateCatchEvent)
                .when(IntermediateThrowEvent.class, intermediateThrowEventConverter::convert)
                .when(Task.class, taskConverter::convert)
                .when(Gateway.class, gatewayConverter::convert)
                .when(SubProcess.class, processConverter::convertSubProcess)
                .when(CallActivity.class, callActivityConverter::convert)
                .ignore(SequenceFlow.class)
                .apply(flowElement);
    }
}
