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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.DefinitionsBuildingContext;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.ElementContainer;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.SequenceFlowConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.ViewDefinitionConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.lanes.LaneConverter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.ActivityPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.BasePropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.BoundaryEventPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.LanePropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public class ProcessConverterFactory {

    private final DefinitionsBuildingContext context;

    private final ViewDefinitionConverter viewDefinitionConverter;
    private final LaneConverter laneConverter;

    private final SequenceFlowConverter sequenceFlowConverter;

    public ProcessConverterFactory(DefinitionsBuildingContext context) {
        this.context = context;

        this.viewDefinitionConverter =
                new ViewDefinitionConverter(
                        context,
                        this);

        this.laneConverter =
                new LaneConverter();

        this.sequenceFlowConverter =
                new SequenceFlowConverter();
    }

    public SubProcessConverter subProcessConverter() {
        return new SubProcessConverter(context, this);
    }

    public void convertChildNodes(
            ElementContainer p,
            Stream<? extends Node<View<? extends BPMNViewDefinition>, ?>> nodes,
            Stream<? extends Node<View<? extends BPMNViewDefinition>, ?>> lanes) {
        nodes.map(viewDefinitionConverter::toFlowElement)
                .filter(Result::notIgnored)
                .map(Result::value)
                .forEach(p::addChildElement);

        convertLanes(lanes, p);
    }

    private void convertLanes(
            Stream<? extends Node<View<? extends BPMNViewDefinition>, ?>> lanes,
            ElementContainer p) {
        List<LanePropertyWriter> collect = lanes
                .map(laneConverter::toElement)
                .filter(Result::notIgnored)
                .map(Result::value)
                .collect(Collectors.toList());

        p.addLaneSet(collect);
        collect.forEach(p::addChildElement);
    }

    public void convertEdges(ElementContainer p, DefinitionsBuildingContext context) {
        context.childEdges()
                .forEach(e -> {
                    BasePropertyWriter pSrc = p.getChildElement(e.getSourceNode().getUUID());
                    // if it's null, then it's a root: skip it
                    if (pSrc != null) {
                        BasePropertyWriter pTgt = p.getChildElement(e.getTargetNode().getUUID());
                        pTgt.setParent(pSrc);
                    }
                });

        context.dockEdges()
                .forEach(e -> {
                    ActivityPropertyWriter pSrc =
                            (ActivityPropertyWriter) p.getChildElement(e.getSourceNode().getUUID());
                    BoundaryEventPropertyWriter pTgt =
                            (BoundaryEventPropertyWriter) p.getChildElement(e.getTargetNode().getUUID());

                    pTgt.setParentActivity(pSrc);
                });

        context.edges()
                .map(e -> sequenceFlowConverter.toFlowElement(e, p))
                .forEach(p::addChildElement);
    }
}