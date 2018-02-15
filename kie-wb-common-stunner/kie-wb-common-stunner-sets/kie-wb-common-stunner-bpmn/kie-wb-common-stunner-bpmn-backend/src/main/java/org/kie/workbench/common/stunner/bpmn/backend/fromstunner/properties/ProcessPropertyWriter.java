package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Documentation;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.Property;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.jboss.drools.DroolsFactory;
import org.jboss.drools.DroolsPackage;
import org.jboss.drools.MetaDataType;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.DeclarationList;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.AdHoc;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessVariables;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class ProcessPropertyWriter {

    private final Process process;
    private List<BaseElement> baseElements = new ArrayList<>();

    public ProcessPropertyWriter(Process rootLevelProcess) {
        this.process = rootLevelProcess;
    }

    public Process getProcess() {
        return process;
    }

    public void addFlowElement(FlowElement flowElement) {
        process.getFlowElements().add(flowElement);
    }

    public void addAllBaseElements(Collection<BaseElement> baseElements) {
        this.baseElements.addAll(baseElements);
    }

    public List<BaseElement> getBaseElements() {
        return baseElements;
    }

    public void setName(String value) {
        process.setName(value);
    }

    public void setExecutable(Boolean value) {
        process.setIsExecutable(value);
    }

    public void setDocumentation(String documentation) {
        Documentation d = bpmn2.createDocumentation();
        d.setText(asCData(documentation));
        process.getDocumentation().add(d);
    }

    public void setPackage(String value) {
        process.getAnyAttribute().add(
                Attributes.drools("packageName", String.valueOf(value)));
    }

    public void setVersion(String value) {
        process.getAnyAttribute().add(Attributes.drools("version", String.valueOf(value)));

    }

    public void setAdHoc(Boolean adHoc) {
        process.getAnyAttribute().add(Attributes.drools("adHoc", String.valueOf(adHoc)));
    }

    public void setDescription(String value) {
        setMeta("customDescription", value);
    }

    protected void setMeta(
            String attributeId,
            String metaDataValue) {

        if (process != null) {
            MetaDataType eleMetadata = DroolsFactory.eINSTANCE.createMetaDataType();
            eleMetadata.setName(attributeId);
            eleMetadata.setMetaValue(asCData(metaDataValue));

            if (process.getExtensionValues() == null || process.getExtensionValues().isEmpty()) {
                ExtensionAttributeValue extensionElement = Bpmn2Factory.eINSTANCE.createExtensionAttributeValue();
                process.getExtensionValues().add(extensionElement);
            }

            FeatureMap.Entry eleExtensionElementEntry = new EStructuralFeatureImpl.SimpleFeatureMapEntry(
                    (EStructuralFeature.Internal) DroolsPackage.Literals.DOCUMENT_ROOT__META_DATA,
                    eleMetadata);
            process.getExtensionValues().get(0).getValue().add(eleExtensionElementEntry);
        }
    }

    // eww
    protected String asCData(String original) {
        return "<![CDATA[" + original + "]]>";
    }

    public void setProcessVariables(ProcessVariables processVariables) {
        String value = processVariables.getValue();
        DeclarationList declarationList = DeclarationList.fromString(value);
        declarationList.getDeclarations().forEach(decl -> {
            ItemDefinition typeDef = bpmn2.createItemDefinition();
            typeDef.setId("_"+decl.getIdentifier()+"Item");
            typeDef.setStructureRef(decl.getType());

            Property property = bpmn2.createProperty();
            property.setId(decl.getIdentifier());
            property.setItemSubjectRef(typeDef);

            process.getProperties().add(property);
            this.addBaseElement(typeDef);

        });
    }

    private void addBaseElement(BaseElement typeDef) {
        this.baseElements.add(typeDef);
    }
}
