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

package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Documentation;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.jboss.drools.DroolsFactory;
import org.jboss.drools.DroolsPackage;
import org.jboss.drools.MetaDataType;
import org.kie.workbench.common.stunner.core.graph.content.Bounds;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;
import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.dc;
import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.di;

public class PropertyWriter {

    protected final FlowElement flowElement;
    private final Map<String, BaseElement> baseElements = new HashMap<>();
    private final BPMNShape shape;

    public PropertyWriter(FlowElement flowElement) {
        this.flowElement = flowElement;
        this.shape = di.createBPMNShape();
        shape.setBpmnElement(flowElement);
    }

    public void setBounds(Bounds rect) {
        org.eclipse.dd.dc.Bounds bounds = dc.createBounds();

        Bounds.Bound upperLeft = rect.getUpperLeft();
        Bounds.Bound lowerRight = rect.getLowerRight();

        bounds.setX(upperLeft.getX().floatValue());
        bounds.setY(upperLeft.getY().floatValue());
        bounds.setWidth(lowerRight.getX().floatValue() - upperLeft.getX().floatValue());
        bounds.setHeight(lowerRight.getY().floatValue() - upperLeft.getY().floatValue());

        shape.setBounds(bounds);
    }

    public FlowElement getFlowElement() {
        return flowElement;
    }

    public void setName(String value) {
        flowElement.setName(value.trim());
        setMeta("elementname", value);
    }

    public void addBaseElement(BaseElement element) {
        this.baseElements.put(element.getId(), element);
    }

    public Collection<BaseElement> getBaseElements() {
        return this.baseElements.values();
    }

    protected void setMeta(
            String attributeId,
            String metaDataValue) {

        if (flowElement != null) {
            MetaDataType eleMetadata = DroolsFactory.eINSTANCE.createMetaDataType();
            eleMetadata.setName(attributeId);
            eleMetadata.setMetaValue(asCData(metaDataValue));

            if (flowElement.getExtensionValues() == null || flowElement.getExtensionValues().isEmpty()) {
                ExtensionAttributeValue extensionElement = Bpmn2Factory.eINSTANCE.createExtensionAttributeValue();
                flowElement.getExtensionValues().add(extensionElement);
            }

            FeatureMap.Entry eleExtensionElementEntry = new EStructuralFeatureImpl.SimpleFeatureMapEntry(
                    (EStructuralFeature.Internal) DroolsPackage.Literals.DOCUMENT_ROOT__META_DATA,
                    eleMetadata);
            flowElement.getExtensionValues().get(0).getValue().add(eleExtensionElementEntry);
        }
    }

    // eww
    protected String asCData(String original) {
        return "<![CDATA[" + original + "]]>";
    }

    public void setDocumentation(String value) {
        Documentation documentation = bpmn2.createDocumentation();
        documentation.setText(asCData(value));
        flowElement.getDocumentation().add(documentation);
    }

    public BPMNShape getShape() {
        return shape;
    }
}
