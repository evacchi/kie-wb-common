package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import org.eclipse.bpmn2.Assignment;
import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.DataOutputAssociation;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.Property;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssociationDeclaration;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class IOPropertyWriter extends PropertyWriter {

    public IOPropertyWriter(FlowElement flowElement) {
        super(flowElement);
    }

    protected DataInputAssociation addDataInputAssociation(AssociationDeclaration.Input declaration) {
        AssociationDeclaration.Pair pair = declaration.getPair();
        return addInputSourceTarget((AssociationDeclaration.SourceTarget) pair);
    }

    protected DataOutputAssociation addOutputSourceTarget(AssociationDeclaration.SourceTarget a) {
        return null;
    }

    protected DataInputAssociation addInputSourceTarget(AssociationDeclaration.SourceTarget a) {
        // first we declare the type of this assignment
        ItemDefinition typeDef =
                typedef(a.getSource(),
                        "java.lang.String");

        // then we declare a name (a variable) with that type,
        // e.g. foo:java.lang.String
        Property decl = varDecl(a.getSource(), typeDef);

        // then we declare the input that will provide
        // the value that we assign to `source`
        // e.g. myInput
        DataInput target = readInputFrom(a.getTarget());

        // then we create the actual association between the two
        // e.g. foo := myInput (or, to put it differently, myInput -> foo)
        DataInputAssociation dataInputAssociation =
                associate(decl, target);
        this.addBaseElement(typeDef);

        return dataInputAssociation;
    }

    private DataInputAssociation addInputFromTo(String attributeId, String value) {
        // first we declare the type of this assignment
        ItemDefinition typeDef =
                typedef(attributeId,
                        "java.lang.String");

        Property decl = varDecl(attributeId, typeDef);

        // then we declare the input that will provide
        // the value that we assign to `source`
        // e.g. myInput
        DataInput target = readInputFrom(attributeId);

        // then we create the actual association between the two
        // e.g. foo := myInput (or, to put it differently, myInput -> foo)
        DataInputAssociation dataInputAssociation =
                associate(decl, target);

        this.addBaseElement(typeDef);

        return dataInputAssociation;
    }

    private Assignment assignment(String from, String to) {
        Assignment assignment = bpmn2.createAssignment();
        FormalExpression fromExpr = bpmn2.createFormalExpression();
        fromExpr.setBody(from);
        assignment.setFrom(fromExpr);
        FormalExpression toExpr = bpmn2.createFormalExpression();
        toExpr.setBody(to);
        assignment.setTo(toExpr);
        return assignment;
    }

    private DataInputAssociation associate(Assignment assignment, DataInput dataInput) {
        DataInputAssociation dataInputAssociation =
                bpmn2.createDataInputAssociation();

        dataInputAssociation.getAssignment()
                .add(assignment);

        dataInputAssociation
                .setTargetRef(dataInput);
        return dataInputAssociation;
    }

    private DataInputAssociation associate(Property source, DataInput dataInput) {
        DataInputAssociation dataInputAssociation =
                bpmn2.createDataInputAssociation();

        dataInputAssociation
                .getSourceRef()
                .add(source);

        dataInputAssociation
                .setTargetRef(dataInput);
        return dataInputAssociation;
    }

    private DataInput readInputFrom(String targetName) {
        DataInput dataInput = bpmn2.createDataInput();
        dataInput.setName(targetName);
        // the id is an encoding of the node id + the name of the input
        dataInput.setId(makeDataInputId(targetName));
        return dataInput;
    }

    private String makeDataInputId(String targetName) {
        return getFlowElement().getId() + "_" + targetName + "InputX";
    }

    private Property varDecl(String varName, ItemDefinition typeDef) {
        Property source = bpmn2.createProperty();
        source.setId(varName);
        source.setItemSubjectRef(typeDef);
        return source;
    }

    private ItemDefinition typedef(String sourceName, String type) {
        ItemDefinition typeDef = bpmn2.createItemDefinition();
        typeDef.setId("_" + sourceName + "Item");
        typeDef.setStructureRef(type);
        return typeDef;
    }
}
