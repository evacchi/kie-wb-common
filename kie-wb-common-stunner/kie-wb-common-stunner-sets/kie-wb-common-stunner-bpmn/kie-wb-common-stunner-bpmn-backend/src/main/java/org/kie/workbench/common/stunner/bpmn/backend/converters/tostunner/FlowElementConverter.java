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

public class FlowElementConverter {

    private final BaseConverterFactory converterFactory;

    public FlowElementConverter(BaseConverterFactory converterFactory) {
        this.converterFactory = converterFactory;
    }

    public Result<BpmnNode> convertNode(FlowElement flowElement) {

        if (flowElement instanceof StartEvent) {
            return Result.success(converterFactory.startEventConverter().convert((StartEvent) flowElement));
        }
        if (flowElement instanceof EndEvent) {
            return Result.success(converterFactory.endEventConverter().convert((EndEvent) flowElement));
        }
        if (flowElement instanceof BoundaryEvent) {
            return Result.success(converterFactory.intermediateCatchEventConverter().convertBoundaryEvent((BoundaryEvent) flowElement));
        }
        if (flowElement instanceof IntermediateCatchEvent) {
            return Result.success(converterFactory.intermediateCatchEventConverter().convertIntermediateCatchEvent((IntermediateCatchEvent) flowElement));
        }
        if (flowElement instanceof IntermediateThrowEvent) {
            return Result.success(converterFactory.intermediateThrowEventConverter().convert((IntermediateThrowEvent) flowElement));
        }
        if (flowElement instanceof Task) {
            return Result.success(converterFactory.taskConverter().convert((Task) flowElement));
        }
        if (flowElement instanceof Gateway) {
            return Result.success(converterFactory.gatewayConverter().convert((Gateway) flowElement));
        }
        if (flowElement instanceof SubProcess) {
            return Result.success(converterFactory.subProcessConverter().convertSubProcess((SubProcess) flowElement));
        }
        if (flowElement instanceof CallActivity) {
            return Result.success(converterFactory.callActivityConverter().convert((CallActivity) flowElement));
        }
        if (flowElement instanceof SequenceFlow) {
            return Result.ignored("sequence flow");
        }
        throw new UnsupportedOperationException();
    }
}
