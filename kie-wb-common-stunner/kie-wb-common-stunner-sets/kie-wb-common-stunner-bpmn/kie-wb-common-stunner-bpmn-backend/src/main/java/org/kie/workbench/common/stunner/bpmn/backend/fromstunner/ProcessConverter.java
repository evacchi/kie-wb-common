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

import java.util.List;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.bpmn2.di.BpmnDiFactory;
import org.eclipse.dd.dc.DcFactory;
import org.eclipse.dd.di.DiagramElement;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;

public class ProcessConverter {

    private static final Bpmn2Factory bpmn2 = Bpmn2Factory.eINSTANCE;
    private static final BpmnDiFactory di = BpmnDiFactory.eINSTANCE;
    private static final DcFactory dc = DcFactory.eINSTANCE;

    private final DefinitionsBuildingContextHelper context;

    private final SequenceFlowUnconverter sequenceFlowUnconverter;
    private final ViewDefinitionConverter viewDefinitionConverter;

    public ProcessConverter(DefinitionsBuildingContextHelper context) {
        this.context = context;
        this.sequenceFlowUnconverter = new SequenceFlowUnconverter(context);
        this.viewDefinitionConverter = new ViewDefinitionConverter(context);
    }

    public Process toFlowElement() {
        Process rootLevelProcess = bpmn2.createProcess();
        rootLevelProcess.setId(context.getGraph().getUUID());

        List<FlowElement> flowElements = rootLevelProcess.getFlowElements();

        context.nodes()
                .map(viewDefinitionConverter::toFlowElement)
                .filter(Result::notIgnored)
                .map(Result::value)
                .forEach(context::addFlowNode);

        context.edges()
                .map(sequenceFlowUnconverter::toFlowElement)
                .forEach(flowElements::add);

        flowElements.addAll(context.getFlowNodes());

        return rootLevelProcess;
    }

    public BPMNDiagram toBPMNDiagram() {
        BPMNDiagram bpmnDiagram = di.createBPMNDiagram();
        bpmnDiagram.setId(context.firstNode().getUUID());

        BPMNPlane bpmnPlane = di.createBPMNPlane();
        bpmnDiagram.setPlane(bpmnPlane);

        List<DiagramElement> planeElement =
                bpmnPlane.getPlaneElement();

        context.nodes()
                .map(viewDefinitionConverter::shapeFrom)
                .forEach(planeElement::add);

        context.edges()
                .map(viewDefinitionConverter::edgeFrom)
                .forEach(planeElement::add);

        return bpmnDiagram;
    }
}