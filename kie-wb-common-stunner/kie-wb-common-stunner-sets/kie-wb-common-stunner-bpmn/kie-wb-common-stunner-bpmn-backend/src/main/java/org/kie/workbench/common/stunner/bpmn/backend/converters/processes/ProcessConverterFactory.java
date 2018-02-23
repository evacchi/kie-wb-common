package org.kie.workbench.common.stunner.bpmn.backend.converters.processes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.LaneSet;
import org.kie.workbench.common.stunner.bpmn.backend.converters.BpmnNode;
import org.kie.workbench.common.stunner.bpmn.backend.converters.DefinitionResolver;
import org.kie.workbench.common.stunner.bpmn.backend.converters.EdgeConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.FlowElementConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.lanes.LaneConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.PropertyReaderFactory;

public class ProcessConverterFactory {

    private final TypedFactoryManager factoryManager;
    private final PropertyReaderFactory propertyReaderFactory;
    private final FlowElementConverter flowElementConverter;
    private final LaneConverter laneConverter;
    private final EdgeConverter edgeConverter;
    private final DefinitionResolver definitionResolver;

    public ProcessConverterFactory(
            TypedFactoryManager typedFactoryManager,
            DefinitionResolver definitionResolver) {

        this.factoryManager = typedFactoryManager;
        this.definitionResolver = definitionResolver;
        this.propertyReaderFactory =
                new PropertyReaderFactory(definitionResolver);

        this.flowElementConverter =
                new FlowElementConverter(
                        factoryManager,
                        propertyReaderFactory,
                        this);

        this.laneConverter =
                new LaneConverter(
                        typedFactoryManager,
                        propertyReaderFactory);

        this.edgeConverter =
                new EdgeConverter(
                        factoryManager,
                        propertyReaderFactory);
    }

    public SubProcessConverter subProcessConverter() {
        return new SubProcessConverter(
                factoryManager,
                propertyReaderFactory,
                this);
    }

    public ProcessConverter processConverter() {
        return new ProcessConverter(
                factoryManager,
                propertyReaderFactory,
                definitionResolver,
                this);
    }

    Map<String, BpmnNode> convertChildNodes(
            BpmnNode firstNode,
            List<FlowElement> flowElements,
            List<LaneSet> laneSets) {

        Map<String, BpmnNode> freeFloatingNodes =
                convertFlowElements(flowElements);

        freeFloatingNodes.values()
                .forEach(n -> n.setParent(firstNode));

        convertLaneSets(laneSets, freeFloatingNodes, firstNode);

        return freeFloatingNodes;
    }

    void convertEdges(BpmnNode processRoot, List<FlowElement> flowElements, Map<String, BpmnNode> nodes) {
        flowElements.stream()
                .map(e -> edgeConverter.convertEdge(e, nodes))
                .filter(Result::isSuccess)
                .map(Result::value)
                .forEach(processRoot::addEdge);
    }

    private Map<String, BpmnNode> convertFlowElements(List<FlowElement> flowElements) {
        LinkedHashMap<String, BpmnNode> result = new LinkedHashMap<>();

        flowElements
                .stream()
                .map(flowElementConverter::convertNode)
                .filter(Result::isSuccess)
                .map(Result::value)
                .forEach(n -> result.put(n.value().getUUID(), n));

        return result;
    }

    private void convertLaneSets(List<LaneSet> laneSets, Map<String, BpmnNode> freeFloatingNodes, BpmnNode firstDiagramNode) {
        laneSets.stream()
                .flatMap(laneSet -> laneSet.getLanes().stream())
                .forEach(lane -> {
                    BpmnNode laneNode = laneConverter.convert(lane);
                    laneNode.setParent(firstDiagramNode);

                    lane.getFlowNodeRefs().forEach(node -> {
                        freeFloatingNodes.get(node.getId()).setParent(laneNode);
                    });
                });
    }
}