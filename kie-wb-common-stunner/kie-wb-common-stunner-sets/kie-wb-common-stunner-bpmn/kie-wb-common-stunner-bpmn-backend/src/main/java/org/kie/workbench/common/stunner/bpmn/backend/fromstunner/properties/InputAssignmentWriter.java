package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.InputSet;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.Property;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.Attribute;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssociationDeclaration;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class InputAssignmentWriter {

    private final String parentId;
    private final AssociationDeclaration associationDeclaration;
    private final DataInputAssociation association;
    private final Property decl;
    private final InputSet inputSet;
    private final DataInput target;
    private ItemDefinition typeDef;

    public InputAssignmentWriter(
            String parentId,
            AssociationDeclaration associationDeclaration,
            String type) {
        this.parentId = parentId;

        this.associationDeclaration = associationDeclaration;

        // first we declare the type of this assignment
        this.typeDef = typedefInput(type);

        // then we declare a name (a variable) with that type,
        // e.g. mySource:java.lang.String
        this.decl = varDecl(associationDeclaration.getLeft(), typeDef);

        // then we declare the input that will provide
        // the value that we assign to `source`
        // e.g. myTarget
        this.target = readInputFrom(associationDeclaration.getRight(), typeDef);

        // then we create the actual association between the two
        // e.g. mySource := myTarget (or, to put it differently, myTarget -> mySource)
        this.association = associationOf(decl, target);

        this.inputSet = bpmn2.createInputSet();
        this.inputSet.getDataInputRefs().add(target);
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

    private Property varDecl(String varName, ItemDefinition typeDef) {
        Property source = bpmn2.createProperty();
        source.setId(varName);
        source.setItemSubjectRef(typeDef);
        return source;
    }

    private ItemDefinition typedefInput(String type) {
        ItemDefinition typeDef = bpmn2.createItemDefinition();
        typeDef.setId(itemId());
        typeDef.setStructureRef(type);
        return typeDef;
    }

    private String dataInputId() {
        return parentId + "_" + associationDeclaration.getRight() + "InputX";
    }

    private String itemId() {
        return "_" + parentId + "_" + associationDeclaration.getRight() + "InputXItem";
    }

    public Property getProperty() {
        return decl;
    }

    public DataInput getDataInput() {
        return target;
    }

    public InputSet getInputSet() {
        return inputSet;
    }

    public DataInputAssociation getAssociation() {
        return association;
    }

    public ItemDefinition getItemDefinition() {
        return typeDef;
    }
}
