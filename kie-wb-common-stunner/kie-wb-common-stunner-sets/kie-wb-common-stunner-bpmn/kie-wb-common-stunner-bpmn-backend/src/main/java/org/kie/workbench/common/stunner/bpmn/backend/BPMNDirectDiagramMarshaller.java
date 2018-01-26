/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.bpmn.backend;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import bpsim.impl.BpsimPackageImpl;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.jboss.drools.DroolsPackage;
import org.jboss.drools.impl.DroolsPackageImpl;
import org.kie.workbench.common.stunner.bpmn.BPMNDefinitionSet;
import org.kie.workbench.common.stunner.bpmn.backend.converters.DefinitionResolver;
import org.kie.workbench.common.stunner.bpmn.backend.converters.DiagramConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.FlowElementConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.GraphBuildingContext;
import org.kie.workbench.common.stunner.bpmn.backend.converters.LaneConverter;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Layout;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Result;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.legacy.resource.JBPMBpmn2ResourceFactoryImpl;
import org.kie.workbench.common.stunner.bpmn.backend.legacy.resource.JBPMBpmn2ResourceImpl;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagramImpl;
import org.kie.workbench.common.stunner.core.api.DefinitionManager;
import org.kie.workbench.common.stunner.core.api.FactoryManager;
import org.kie.workbench.common.stunner.core.definition.service.DiagramMarshaller;
import org.kie.workbench.common.stunner.core.definition.service.DiagramMetadataMarshaller;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.command.EmptyRulesCommandExecutionContext;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandManager;
import org.kie.workbench.common.stunner.core.graph.command.impl.GraphCommandFactory;
import org.kie.workbench.common.stunner.core.graph.content.definition.DefinitionSet;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.processing.index.map.MapIndex;
import org.kie.workbench.common.stunner.core.graph.processing.index.map.MapIndexBuilder;
import org.kie.workbench.common.stunner.core.rule.RuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Direct as in "skipping json encoding"
 *
 */
public class BPMNDirectDiagramMarshaller<D> implements DiagramMarshaller<Graph, Metadata, Diagram<Graph, Metadata>> {

    private static final Logger LOG = LoggerFactory.getLogger(BPMNDirectDiagramMarshaller.class);

    private final DefinitionManager definitionManager;
    private final RuleManager ruleManager;
    private final TypedFactoryManager typedFactoryManager;
    private final GraphCommandFactory commandFactory;
    private final GraphCommandManager commandManager;

    public BPMNDirectDiagramMarshaller(
            final DefinitionManager definitionManager,
            final RuleManager ruleManager,
            final FactoryManager factoryManager,
            final GraphCommandFactory commandFactory,
            final GraphCommandManager commandManager) {
        this.definitionManager = definitionManager;
        this.ruleManager = ruleManager;
        this.typedFactoryManager = new TypedFactoryManager(factoryManager);
        this.commandFactory = commandFactory;
        this.commandManager = commandManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String marshall(final Diagram diagram) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Graph<DefinitionSet, Node> unmarshall(final Metadata metadata,
                                                 final InputStream inputStream) throws IOException {
        LOG.debug("Starting diagram unmarshalling...");

        Definitions definitions = parseDefinitions(inputStream);
        DefinitionResolver definitionResolver =
                new DefinitionResolver(definitions);

        DiagramConverter diagramConverter =
                new DiagramConverter(typedFactoryManager);
        FlowElementConverter flowElementConverter =
                new FlowElementConverter(typedFactoryManager, definitionResolver);
        LaneConverter laneconverter =
                new LaneConverter(typedFactoryManager, definitionResolver);

        Process process = findProcess(definitions);

        metadata.setCanvasRootUUID(definitions.getId());
        metadata.setTitle(process.getName());

        Graph<DefinitionSet, Node> graph = graphOf(process.getId());

        BPMNPlane plane = findPlane(definitions);
        Layout layout = new Layout(plane);
        GraphBuildingContext context = graphContextOf(graph);
        context.clearGraph();

        Node<View<BPMNDiagramImpl>, ?> firstDiagramNode =
                diagramConverter.convert(definitions.getId(), process);

        context.addNode(firstDiagramNode);

        process.getFlowElements()
                .stream()
                .map(flowElementConverter::convertNode)
                .filter(Result::notIgnored)
                .map(Result::value)
                .forEach(n -> {
                    layout.updateNode(n);
                    context.addNode(n);
                });

        process.getLaneSets()
                .stream()
                .flatMap(laneSet -> laneSet.getLanes().stream())
                .map(laneconverter::convert)
                .forEach(n -> {
                    layout.updateNode(n);
                    context.addNode(n);
                });

        process.getFlowElements()
                .stream()
                .map(e -> flowElementConverter.convertEdge(e, context))
                .filter(Result::isSuccess)
                .map(Result::value)
                .forEach(layout::updateEdge);

        process.getFlowElements()
                .forEach(e -> flowElementConverter.convertDockedNodes(e, context));

        LOG.debug("Diagram unmarshalling finished successfully.");
        return graph;
    }

    private Graph<DefinitionSet, Node> graphOf(String id) {
        return typedFactoryManager.newGraph(id, BPMNDefinitionSet.class);
    }

    public BPMNPlane findPlane(Definitions definitions) {
        return definitions.getDiagrams().get(0).getPlane();
    }

    public Process findProcess(Definitions definitions) {
        return (Process) definitions.getRootElements().stream()
                .filter(el -> el instanceof Process)
                .findFirst().get();
    }

    private GraphBuildingContext graphContextOf(Graph<DefinitionSet, Node> graph) {
        return new GraphBuildingContext(createExecutionContext(graph), commandFactory, commandManager);
    }

    private MapIndex createMapIndex(Graph<DefinitionSet, Node> graph) {
        MapIndexBuilder builder = new MapIndexBuilder();
        return builder.build(graph);
    }

    private EmptyRulesCommandExecutionContext createExecutionContext(Graph<DefinitionSet, Node> graph) {
        return new EmptyRulesCommandExecutionContext(definitionManager,
                                                     typedFactoryManager.untyped(),
                                                     ruleManager,
                                                     createMapIndex(graph));
    }

    @Override
    public DiagramMetadataMarshaller<Metadata> getMetadataMarshaller() {
        return null;
    }

    public static Definitions parseDefinitions(final InputStream inputStream) throws IOException {
        DroolsPackageImpl.init();
        BpsimPackageImpl.init();

        final ResourceSet resourceSet = new ResourceSetImpl();
        Resource.Factory.Registry resourceFactoryRegistry = resourceSet.getResourceFactoryRegistry();
        resourceFactoryRegistry.getExtensionToFactoryMap().put(
                Resource.Factory.Registry.DEFAULT_EXTENSION, new JBPMBpmn2ResourceFactoryImpl());

        EPackage.Registry packageRegistry = resourceSet.getPackageRegistry();
        packageRegistry.put("http://www.omg.org/spec/BPMN/20100524/MODEL", Bpmn2Package.eINSTANCE);
        packageRegistry.put("http://www.jboss.org/drools", DroolsPackage.eINSTANCE);

        final JBPMBpmn2ResourceImpl resource =
                (JBPMBpmn2ResourceImpl) resourceSet
                        .createResource(URI.createURI("inputStream://dummyUriWithValidSuffix.xml"));

        resource.getDefaultLoadOptions()
                .put(JBPMBpmn2ResourceImpl.OPTION_ENCODING, "UTF-8");
        resource.setEncoding("UTF-8");

        final Map<String, Object> options = new HashMap<String, Object>();
        options.put(JBPMBpmn2ResourceImpl.OPTION_ENCODING, "UTF-8");
        options.put(JBPMBpmn2ResourceImpl.OPTION_DEFER_IDREF_RESOLUTION, true);
        options.put(JBPMBpmn2ResourceImpl.OPTION_DISABLE_NOTIFY, true);
        options.put(JBPMBpmn2ResourceImpl.OPTION_PROCESS_DANGLING_HREF,
                    JBPMBpmn2ResourceImpl.OPTION_PROCESS_DANGLING_HREF_RECORD);

        resource.load(inputStream, options);

        final DocumentRoot root = (DocumentRoot) resource.getContents().get(0);

        return root.getDefinitions();
    }
}
