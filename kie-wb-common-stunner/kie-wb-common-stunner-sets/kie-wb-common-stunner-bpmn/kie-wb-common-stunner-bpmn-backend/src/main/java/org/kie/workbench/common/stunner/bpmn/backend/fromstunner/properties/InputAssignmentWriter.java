package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.InputSet;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.Property;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.Attribute;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.VariableDeclaration;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class InputAssignmentWriter {

    private final String parentId;
    private final DataInputAssociation association;
    private final InputSet inputSet;
    private final DataInput target;
    private final ItemDefinition typeDef;
    private final VariableDeclaration decl;

    public InputAssignmentWriter(
            String parentId,
            VariableScope.Variable variable,
            VariableDeclaration decl) {
        this.parentId = parentId;
        this.decl = decl;

        this.typeDef = typedefInput(decl);

        // then we declare the input that will provide
        // the value that we assign to `source`
        // e.g. myTarget
        this.target = readInputFrom(decl.getIdentifier(), typeDef);

        // then we create the actual association between the two
        // e.g. mySource := myTarget (or, to put it differently, myTarget -> mySource)
        this.association = associationOf(variable.getTypedIdentifier(), target);

        this.inputSet = bpmn2.createInputSet();
        this.inputSet.getDataInputRefs().add(target);
    }

    public ItemDefinition typedefInput(VariableDeclaration decl) {
        ItemDefinition typeDef = bpmn2.createItemDefinition();
        typeDef.setId(itemId());
        typeDef.setStructureRef(decl.getType());
        return typeDef;
    }

    private DataInputAssociation associationOf(Property source, DataInput dataInput) {
        DataInputAssociation dataInputAssociation =
                bpmn2.createDataInputAssociation();

        dataInputAssociation
                .getSourceRef()
                .add(source);

        dataInputAssociation
                .setTargetRef(dataInput);
        return dataInputAssociation;
    }

    private DataInput readInputFrom(String targetName, ItemDefinition typeDef) {
        DataInput dataInput = bpmn2.createDataInput();
        // the id is an encoding of the node id + the name of the input
        dataInput.setId(dataInputId());
        dataInput.setName(targetName);
        dataInput.setItemSubjectRef(typeDef);
        Attribute.dtype.of(dataInput).set(typeDef.getStructureRef());
        return dataInput;
    }

    private String dataInputId() {
        return parentId + "_" + decl.getIdentifier() + "InputX";
    }

    private String itemId() {
        return "_" + dataInputId() + "Item";
    }

    private String propertyId(String id) {
        return "prop" + id + dataInputId();
    }

    public DataInput getDataInput() {
        return target;
    }

    public ItemDefinition getItemDefinition() {
        return typeDef;
    }

    public InputSet getInputSet() {
        return inputSet;
    }

    public DataInputAssociation getAssociation() {
        return association;
    }
}
