/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.standalone.client.services;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;
import elemental2.promise.Promise;
import org.eclipse.bpmn2.AdHocSubProcess;
import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.emf.ecore.EObject;
import org.jboss.drools.DroolsFactory;
import org.jboss.drools.OnEntryScriptType;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.stunner.bpmn.BPMNDefinitionSet;
import org.kie.workbench.common.stunner.bpmn.backend.converters.TypedFactoryManager;
import org.kie.workbench.common.stunner.bpmn.backend.converters.fromstunner.properties.PropertyWriterFactory;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.BpmnNode;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.ConverterFactory;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.DefinitionResolver;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.GraphBuilder;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties.PropertyReaderFactory;
import org.kie.workbench.common.stunner.core.api.DefinitionManager;
import org.kie.workbench.common.stunner.core.api.FactoryManager;
import org.kie.workbench.common.stunner.core.client.api.ShapeManager;
import org.kie.workbench.common.stunner.core.client.service.ServiceCallback;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.diagram.MetadataImpl;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.command.GraphCommandManager;
import org.kie.workbench.common.stunner.core.graph.command.impl.GraphCommandFactory;
import org.kie.workbench.common.stunner.core.graph.content.definition.DefinitionSet;
import org.kie.workbench.common.stunner.core.rule.RuleManager;
import org.kie.workbench.common.stunner.core.util.StringUtils;
import org.kie.workbench.common.stunner.standalone.client.marshalling.Bpmn2Resource;
import org.kie.workbench.common.stunner.submarine.api.diagram.SubmarineDiagram;
import org.kie.workbench.common.stunner.submarine.api.editor.DiagramType;
import org.kie.workbench.common.stunner.submarine.api.editor.impl.SubmarineDiagramResourceImpl;
import org.kie.workbench.common.stunner.submarine.api.service.SubmarineDiagramService;
import org.kie.workbench.common.stunner.submarine.client.service.SubmarineClientDiagramService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.VFSService;
import org.uberfire.client.promise.Promises;

@ApplicationScoped
public class BPMNStandaloneClientDiagramServiceImpl implements SubmarineClientDiagramService {

    private ShapeManager shapeManager;
    private Caller<VFSService> vfsServiceCaller;
    private Caller<SubmarineDiagramService> submarineDiagramServiceCaller;
    private Promises promises;

    public BPMNStandaloneClientDiagramServiceImpl() {
        //CDI proxy
    }

    @Inject
    public BPMNStandaloneClientDiagramServiceImpl(final ShapeManager shapeManager,
                                                  final Caller<VFSService> vfsServiceCaller,
                                                  final Caller<SubmarineDiagramService> submarineDiagramServiceCaller,
                                                  final Promises promises) {
        this.shapeManager = shapeManager;
        this.vfsServiceCaller = vfsServiceCaller;
        this.submarineDiagramServiceCaller = submarineDiagramServiceCaller;
        this.promises = promises;
    }

    public void saveAsXml(final Path path,
                          final String xml,
                          final ServiceCallback<String> callback) {
        vfsServiceCaller.call((Path p) -> {
            callback.onSuccess(xml);
        }).write(path, xml);
    }

    public void loadAsXml(final Path path,
                          final ServiceCallback<String> callback) {
        vfsServiceCaller.call((RemoteCallback<String>) callback::onSuccess).readAllString(path);
    }

    //Submarine requirements

    @Override
    public void transform(final String xml,
                          final ServiceCallback<SubmarineDiagram> callback) {

        AdHocSubProcess clientside = Bpmn2Factory.eINSTANCE.createAdHocSubProcess();
        OnEntryScriptType onEntryScriptType = DroolsFactory.eINSTANCE.createOnEntryScriptType();
        Bpmn2Resource bpmn2Resource = new Bpmn2Resource();
        try {
            bpmn2Resource.load(xml);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        DocumentRoot docRoot = (DocumentRoot) bpmn2Resource.getContents().get(0);
        DefinitionResolver definitionResolver = new DefinitionResolver(docRoot.getDefinitions(), Collections.emptyList());
        // fixme this should come from outside
        FactoryManager factoryManager = null;
        TypedFactoryManager typedFactoryManager = new TypedFactoryManager(factoryManager);
        ConverterFactory converterFactory = new ConverterFactory(definitionResolver, typedFactoryManager);
        BpmnNode diagramRoot = converterFactory.rootProcessConverter().convertProcess();

        // fixme: should it come from outside?
        Metadata metadata = new MetadataImpl();

        // fixme, this comes from outside (see BaseDirectDiagramMarshaller)
        DefinitionManager definitionManager = null;
        RuleManager ruleManager = null;
        GraphCommandFactory commandFactory = null;
        GraphCommandManager commandManager = null;

        // the root node contains all of the information
        // needed to build the entire graph (including parent/child relationships)
        // thus, we can now walk the graph to issue all the commands
        // to draw it on our canvas
        Diagram<Graph<DefinitionSet, Node>, Metadata> diagram =
                typedFactoryManager.newDiagram(
                        definitionResolver.getDefinitions().getId(),
                        BPMNDefinitionSet.class,
                        metadata);
        GraphBuilder graphBuilder =
                new GraphBuilder(
                        diagram.getGraph(),
                        definitionManager,
                        typedFactoryManager,
                        ruleManager,
                        commandFactory,
                        commandManager);
        graphBuilder.render(diagramRoot);

        GWT.log(clientside.toString());
        GWT.log(onEntryScriptType.toString());
        GWT.log(diagramRoot.toString());



        submarineDiagramServiceCaller.call((SubmarineDiagram d) -> {
            updateClientMetadata(d);
            callback.onSuccess(d);
        }).transform(xml);
    }

    @Override
    public Promise<String> transform(final SubmarineDiagramResourceImpl resource) {
        if (resource.getType() == DiagramType.PROJECT_DIAGRAM) {
            return promises.promisify(submarineDiagramServiceCaller,
                                      s -> {
                                          return s.transform(resource.projectDiagram().orElseThrow(() -> new IllegalStateException("DiagramType is PROJECT_DIAGRAM however no instance present")));
                                      });
        }
        return promises.resolve(resource.xmlDiagram().orElse("DiagramType is XML_DIAGRAM however no instance present"));
    }

    private void updateClientMetadata(final SubmarineDiagram diagram) {
        if (null != diagram) {
            final Metadata metadata = diagram.getMetadata();
            if (Objects.nonNull(metadata) && StringUtils.isEmpty(metadata.getShapeSetId())) {
                final String sId = shapeManager.getDefaultShapeSet(metadata.getDefinitionSetId()).getId();
                metadata.setShapeSetId(sId);
            }
        }
    }
}
