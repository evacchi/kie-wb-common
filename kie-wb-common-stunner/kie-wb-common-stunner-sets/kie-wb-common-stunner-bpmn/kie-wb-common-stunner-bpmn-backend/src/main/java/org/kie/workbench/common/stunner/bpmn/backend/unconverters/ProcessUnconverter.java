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

package org.kie.workbench.common.stunner.bpmn.backend.unconverters;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.bpmn2.di.BpmnDiFactory;
import org.eclipse.dd.dc.DcFactory;
import org.eclipse.dd.di.DiagramElement;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagramImpl;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.definition.Definition;
import org.kie.workbench.common.stunner.core.graph.content.definition.DefinitionSet;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;

public class ProcessUnconverter {

    private final SequenceFlowUnconverter sequenceFlowUnconverter;

    private static final Bpmn2Factory bpmn2 = Bpmn2Factory.eINSTANCE;
    private static final BpmnDiFactory di = BpmnDiFactory.eINSTANCE;
    private static final DcFactory dc = DcFactory.eINSTANCE;
    private final UnconverterContext context;

    public ProcessUnconverter(UnconverterContext context) {
        this.context = context;
        this.sequenceFlowUnconverter = new SequenceFlowUnconverter(context);
    }

    public Process toFlowElement() {

        FlowElementUnconverter flowElementUnconverter = new FlowElementUnconverter();

        Process rootLevelProcess = bpmn2.createProcess();
//        rootLevelProcess.setId(graph.getUUID());

        List<FlowElement> flowElements = rootLevelProcess.getFlowElements();

        context.nodes()
                .map(flowElementUnconverter::unconvert)
                .filter(Result::notIgnored)
                .map(Result::value)
                .forEach(context::addFlowNode);

        context.edges()
                .map(sequenceFlowUnconverter::toFlowElement)
                .forEach(flowElements::add);

        flowElements.addAll(context.flowNodes.values());

        return rootLevelProcess;
    }

    public BPMNDiagram toBPMNDiagram() {
        BPMNDiagram bpmnDiagram = di.createBPMNDiagram();
        bpmnDiagram.setId(context.firstNode().getUUID());

        BPMNPlane bpmnPlane = di.createBPMNPlane();
        bpmnDiagram.setPlane(bpmnPlane);
        List<DiagramElement> planeElement = bpmnPlane.getPlaneElement();

        BPMNDiagramUnconverter bpmnDiagramUnconverter = new BPMNDiagramUnconverter(context);

        context.nodes()
                .map(bpmnDiagramUnconverter::shapeFrom)
                .forEach(planeElement::add);

        context.edges()
                .map(bpmnDiagramUnconverter::edgeFrom)
                .forEach(planeElement::add);

        return bpmnDiagram;
    }

    private Stream<Node<View<? extends BPMNViewDefinition>, Edge<ViewConnector<BPMNViewDefinition>, ?>>> nodes(
            Graph<DefinitionSet, Node<
                    View<? extends BPMNViewDefinition>,
                    Edge<ViewConnector<BPMNViewDefinition>, ?>>> graph,
            Node<Definition<BPMNDiagramImpl>, ?> firstNode) {
        return StreamSupport
                .stream(graph.nodes().spliterator(), false)
                .filter(n -> !n.getUUID().equals(firstNode.getUUID()));
    }
}