package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import org.eclipse.bpmn2.Assignment;
import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.DataOutput;
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

    protected DataInputAssociation addInputSourceTarget(AssociationDeclaration.SourceTarget a) {
        // first we declare the type of this assignment
        ItemDefinition typeDef =
                typedef(a.getSource(),
                        "java.lang.String");
        typeDef.setId("_"+makeDataInputId(a.getTarget())+"Item");

        // then we declare a name (a variable) with that type,
        // e.g. foo:java.lang.String
        Property decl = varDecl(a.getSource(), typeDef);

        // then we declare the input that will provide
        // the value that we assign to `source`
        // e.g. myInput
        DataInput target = readInputFrom(a.getTarget());
        target.setItemSubjectRef(typeDef);
        target.getAnyAttribute().add(Attributes.drools("dtype", "String"));

        // then we create the actual association between the two
        // e.g. foo := myInput (or, to put it differently, myInput -> foo)
        DataInputAssociation dataInputAssociation =
                associate(decl, target);
        this.addBaseElement(typeDef);

        return dataInputAssociation;
    }

    protected DataInputAssociation input(String attributeId, Object value) {
        // first we declare the type of this assignment
        ItemDefinition typeDef =
                typedef(attributeId,
                        "java.lang.String");

        Property decl = varDecl(attributeId, typeDef);

//        // then we declare the input that will provide
//        // the value that we assign to `source`
//        // e.g. myInput
        DataInput target = readInputFrom(attributeId);

        Assignment assignment = assignment(value.toString(), target.getId());

        // then we create the actual association between the two
        // e.g. foo := myInput (or, to put it differently, myInput -> foo)
        DataInputAssociation dataInputAssociation =
                associate(assignment, target);

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

    private DataOutputAssociation associateOutput(Property source, DataOutput dataOutput) {
        DataOutputAssociation dataInputAssociation =
                bpmn2.createDataOutputAssociation();

        dataInputAssociation
                .getSourceRef()
                .add(dataOutput);

        dataInputAssociation
                .setTargetRef(source);

        return dataInputAssociation;
    }


    private DataInput readInputFrom(String targetName) {
        DataInput dataInput = bpmn2.createDataInput();
        dataInput.setName(targetName);
        // the id is an encoding of the node id + the name of the input
        dataInput.setId(makeDataInputId(targetName));
        return dataInput;
    }

    private DataOutput writeOutputTo(String sourceName) {
        DataOutput dataInput = bpmn2.createDataOutput();
        dataInput.setName(sourceName);
        // the id is an encoding of the node id + the name of the output
        dataInput.setId(makeDataOutputId(sourceName));
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

    public DataOutputAssociation addDataOutputAssociation(AssociationDeclaration.Output declaration) {
        AssociationDeclaration.Pair pair = declaration.getPair();
        return addOutputSourceTarget((AssociationDeclaration.SourceTarget) pair);
    }

    private DataOutputAssociation addOutputSourceTarget(AssociationDeclaration.SourceTarget a) {
        // first we declare the type of this assignment
        ItemDefinition typeDef =
                typedef(a.getTarget(),
                        "java.lang.String");
        typeDef.setId("_"+makeDataOutputId(a.getSource())+"Item");

        // then we declare a name (a variable) with that type,
        // e.g. foo:java.lang.String
        Property decl = varDecl(a.getTarget(), typeDef);

        // then we declare the input that will provide
        // the value that we assign to `source`
        // e.g. myInput
        DataOutput source = writeOutputTo(a.getSource());
        source.setItemSubjectRef(typeDef);
        source.getAnyAttribute().add(Attributes.drools("dtype", "String"));

        // then we create the actual association between the two
        // e.g. foo := myInput (or, to put it differently, myInput -> foo)
        DataOutputAssociation dataOutputAssociation =
                associateOutput(decl, source);
        this.addBaseElement(typeDef);

        return dataOutputAssociation;
    }

    private String makeDataOutputId(String targetName) {
        return getFlowElement().getId() + "_" + targetName + "OutputX";
    }
}
