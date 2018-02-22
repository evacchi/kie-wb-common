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

package org.kie.workbench.common.stunner.bpmn.backend.converters.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.DataOutput;
import org.eclipse.bpmn2.DataOutputAssociation;
import org.eclipse.bpmn2.ItemAwareElement;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentDeclaration;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssociationDeclaration;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssociationList;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.DeclarationList;

public class AssignmentsInfos {

    public static AssignmentsInfo of(
            final List<DataInput> datainput,
//            final List<InputSet> inputSets,
            final List<DataInputAssociation> inputAssociations,
            final List<DataOutput> dataoutput,
//            final List<OutputSet> dataoutputset,
            final List<DataOutputAssociation> outputAssociations,
            boolean alternativeEncoding) {

        DeclarationList inputs = dataInputDeclarations(datainput);
        List<AssociationDeclaration.Input> inputAssociationDeclarations =
                inAssociationDeclarations(inputAssociations);

        DeclarationList outputs = dataOutputDeclarations(dataoutput);
        List<AssociationDeclaration.Output> outputAssociationDeclarations =
                outAssociationDeclarations(outputAssociations);

        AssociationList associations = new AssociationList(
                inputAssociationDeclarations,
                outputAssociationDeclarations);

        return new AssignmentsInfo(inputs, outputs, associations, alternativeEncoding);
    }

    public static DeclarationList dataInputDeclarations(List<DataInput> dataInputs) {
        return new DeclarationList(
                dataInputs.stream()
                        .filter(o -> !o.getName().equals("TaskName"))
                        //.filter(o -> !extractDtype(o).isEmpty())
                        .map(AssignmentsInfos::declarationFromInput)
                        .collect(Collectors.toList()));
    }

    public static DeclarationList dataOutputDeclarations(List<DataOutput> dataInputs) {
        return new DeclarationList(
                dataInputs.stream()
                        .filter(o -> !extractDtype(o).isEmpty())
                        .map(AssignmentsInfos::declarationFromOutput)
                        .collect(Collectors.toList()));
    }

    public static AssignmentDeclaration declarationFromInput(DataInput in) {
        return new AssignmentDeclaration(
                in.getName(),
                extractDtype(in));
    }

    public static AssignmentDeclaration declarationFromOutput(DataOutput out) {
        return new AssignmentDeclaration(
                out.getName(),
                extractDtype(out));
    }

    public static List<AssociationDeclaration.Output> outAssociationDeclarations(List<DataOutputAssociation> outputAssociations) {
        List<AssociationDeclaration.Output> result = new ArrayList<>();
        for (DataOutputAssociation doa : outputAssociations) {
            DataOutput dataOutput = (DataOutput) doa.getSourceRef().get(0);
            String source = dataOutput.getName();
            String target = doa.getTargetRef().getId();
            if (source != null && source.length() > 0) {
                result.add(new AssociationDeclaration.Output(new AssociationDeclaration.SourceTarget(source, target)));
            }
        }
        return result;
    }

    public static List<AssociationDeclaration.Input> inAssociationDeclarations(List<DataInputAssociation> inputAssociations) {
        List<AssociationDeclaration.Input> result = new ArrayList<>();

        for (DataInputAssociation dia : inputAssociations) {
            List<ItemAwareElement> sourceRef = dia.getSourceRef();
            if (sourceRef.isEmpty()) {
                continue;
            }
            String source = sourceRef.get(0).getId();
            String target = ((DataInput) dia.getTargetRef()).getName();
            if (source != null && source.length() > 0) {
                result.add(new AssociationDeclaration.Input(new AssociationDeclaration.SourceTarget(source, target)));
            }
        }

        return result;
    }

    private static String extractDtype(BaseElement el) {
        return getAnyAttributeValue(el, "dtype");
    }

    private static String getAnyAttributeValue(BaseElement el, String attrName) {
        for (FeatureMap.Entry entry : el.getAnyAttribute()) {
            if (attrName.equals(entry.getEStructuralFeature().getName())) {
                return entry.getValue().toString();
            }
        }
        return "";
    }
}
