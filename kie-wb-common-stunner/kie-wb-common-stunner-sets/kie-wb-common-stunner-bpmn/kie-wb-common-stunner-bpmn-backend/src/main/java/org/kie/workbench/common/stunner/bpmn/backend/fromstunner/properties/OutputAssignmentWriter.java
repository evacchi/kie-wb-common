package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import org.eclipse.bpmn2.DataOutput;
import org.eclipse.bpmn2.DataOutputAssociation;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.OutputSet;
import org.eclipse.bpmn2.Property;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.Attribute;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssociationDeclaration;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class OutputAssignmentWriter {

    private final String parentId;
    private final AssociationDeclaration associationDeclaration;
    private final DataOutputAssociation association;
    private final Property decl;
    private final OutputSet outputSet;
    private final DataOutput source;
    private ItemDefinition typeDef;

    public OutputAssignmentWriter(
            String parentId,
            AssociationDeclaration associationDeclaration,
            String type) {
        this.parentId = parentId;

        this.associationDeclaration = associationDeclaration;

        // first we declare the type of this assignment
        this.typeDef = typedefOutput(type);

        // then we declare a name (a variable) with that type,
        // e.g. myTarget:java.lang.String
        this.decl = varDecl(associationDeclaration.getRight(), typeDef);

        // then we declare the input that will provide
        // the value that we assign to `source`
        // e.g. myTarget
        this.source = writeOutputTo(associationDeclaration.getLeft(), typeDef);

        // then we create the actual association between the two
        // e.g. mySource := myTarget (or, to put it differently, myTarget -> mySource)
        this.association = associationOf(decl, source);

        this.outputSet = bpmn2.createOutputSet();
        this.outputSet.getDataOutputRefs().add(source);
    }

    private DataOutputAssociation associationOf(Property source, DataOutput dataOutput) {
        DataOutputAssociation dataOutputAssociation =
                bpmn2.createDataOutputAssociation();

        dataOutputAssociation
                .getSourceRef()
                .add(dataOutput);

        dataOutputAssociation
                .setTargetRef(source);
        return dataOutputAssociation;
    }

    private DataOutput writeOutputTo(String sourceName, ItemDefinition typeDef) {
        DataOutput dataOutput = bpmn2.createDataOutput();
        // the id is an encoding of the node id + the name of the output
        dataOutput.setId(dataOutputId());
        dataOutput.setName(sourceName);
        dataOutput.setItemSubjectRef(typeDef);
        Attribute.dtype.of(dataOutput).set(typeDef.getStructureRef());
        return dataOutput;
    }

    private Property varDecl(String varName, ItemDefinition typeDef) {
        Property source = bpmn2.createProperty();
        source.setId(varName);
        source.setItemSubjectRef(typeDef);
        return source;
    }

    private ItemDefinition typedefOutput(String type) {
        ItemDefinition typeDef = bpmn2.createItemDefinition();
        typeDef.setId(itemId());
        typeDef.setStructureRef(type);
        return typeDef;
    }

    private String dataOutputId() {
        return parentId + "_" + associationDeclaration.getLeft() + "OutputX";
    }

    private String itemId() {
        return "_" + parentId + "_" + associationDeclaration.getLeft() + "OutputXItem";
    }

    public Property getProperty() {
        return decl;
    }

    public DataOutput getDataOutput() {
        return source;
    }

    public OutputSet getOutputSet() {
        return outputSet;
    }

    public DataOutputAssociation getAssociation() {
        return association;
    }

    public ItemDefinition getItemDefinition() {
        return typeDef;
    }
}
