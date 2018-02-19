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

import java.util.Map;

import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.FormalExpression;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.Scripts;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.BasePropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.PropertyWriter;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.SequenceFlowPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.SequenceFlow;
import org.kie.workbench.common.stunner.bpmn.definition.property.connectors.SequenceFlowExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ScriptTypeValue;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewConnector;

import static org.kie.workbench.common.stunner.bpmn.backend.converters.properties.Scripts.asCData;
import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class SequenceFlowConverter {

    private final DefinitionsBuildingContext context;

    public SequenceFlowConverter(DefinitionsBuildingContext context) {
        this.context = context;
    }

    public org.eclipse.bpmn2.SequenceFlow toFlowElement(Edge<?, ?> edge, Map<String, BasePropertyWriter> props) {
        ViewConnector<SequenceFlow> content = (ViewConnector<SequenceFlow>) edge.getContent();
        SequenceFlow definition = content.getDefinition();
        org.eclipse.bpmn2.SequenceFlow seq = bpmn2.createSequenceFlow();
        SequenceFlowPropertyWriter p = new SequenceFlowPropertyWriter(seq);

        seq.setId(edge.getUUID());

        BasePropertyWriter pSrc = props.get(edge.getSourceNode().getUUID());
        BasePropertyWriter pTgt = props.get(edge.getTargetNode().getUUID());

        p.setSource(pSrc);
        p.setTarget(pTgt);


        seq.setId(edge.getUUID());
        seq.setName(definition.getGeneral().getName().getValue());

        p.setAutoConnection(content);

        SequenceFlowExecutionSet executionSet = definition.getExecutionSet();
        ScriptTypeValue scriptTypeValue = executionSet.getConditionExpression().getValue();
        String language = scriptTypeValue.getLanguage();
        String script = scriptTypeValue.getScript();

        if (script != null) {
            FormalExpression formalExpression = bpmn2.createFormalExpression();
            String uri = Scripts.scriptLanguageToUri(language);
            formalExpression.setLanguage(uri);
            formalExpression.setBody(asCData(script));
            seq.setConditionExpression(formalExpression);
        }
        return seq;
    }
}
