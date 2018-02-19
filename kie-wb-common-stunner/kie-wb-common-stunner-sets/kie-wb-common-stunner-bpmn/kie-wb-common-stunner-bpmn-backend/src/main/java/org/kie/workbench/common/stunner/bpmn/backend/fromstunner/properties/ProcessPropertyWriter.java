package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import java.util.Collection;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Documentation;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.Property;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.DeclarationList;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessVariables;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class ProcessPropertyWriter extends BasePropertyWriter {

    private final Process process;

    public ProcessPropertyWriter(Process process) {
        super(process);
        this.process = process;
    }

    public Process getProcess() {
        return process;
    }

    public void addFlowElement(FlowElement flowElement) {
        process.getFlowElements().add(flowElement);
    }

    public void addAllBaseElements(Collection<BaseElement> baseElements) {
        baseElements.forEach(el -> this.baseElements.put(el.getId(), el));
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

    // eww
    protected String asCData(String original) {
        return "<![CDATA[" + original + "]]>";
    }

    public void setProcessVariables(ProcessVariables processVariables) {
        String value = processVariables.getValue();
        DeclarationList declarationList = DeclarationList.fromString(value);
        declarationList.getDeclarations().forEach(decl -> {
            ItemDefinition typeDef = bpmn2.createItemDefinition();
            typeDef.setId("_" + decl.getIdentifier() + "Item");
            typeDef.setStructureRef(decl.getType());

            Property property = bpmn2.createProperty();
            property.setId(decl.getIdentifier());
            property.setItemSubjectRef(typeDef);

            process.getProperties().add(property);
            this.addBaseElement(typeDef);
        });
    }
}
