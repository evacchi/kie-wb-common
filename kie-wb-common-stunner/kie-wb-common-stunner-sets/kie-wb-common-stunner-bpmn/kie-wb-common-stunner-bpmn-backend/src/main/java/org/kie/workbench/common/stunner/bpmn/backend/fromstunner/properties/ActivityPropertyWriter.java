package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.Assignment;
import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataOutput;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.InputOutputSpecification;
import org.eclipse.bpmn2.InputSet;
import org.eclipse.bpmn2.OutputSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class ActivityPropertyWriter extends IOPropertyWriter {

    private final Activity activity;

    public ActivityPropertyWriter(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    public void setAssignmentsInfo(AssignmentsInfo assignmentsInfo) {
        InputOutputSpecification ioSpec = bpmn2.createInputOutputSpecification();
        activity.setIoSpecification(ioSpec);

        assignmentsInfo.getAssociations()
                .getInputs()
                .stream()
                .map(this::addDataInputAssociation)
                .forEach(dia -> {
                    InputSet inputSet = bpmn2.createInputSet();
                    dia.getSourceRef().forEach(this::addBaseElement);
                    this.addBaseElement(dia.getTargetRef());
                    ioSpec.getInputSets().add(inputSet);

                    DataInput targetRef = (DataInput) dia.getTargetRef();
                    inputSet.getDataInputRefs().add(targetRef);
                    ioSpec.getDataInputs().add(targetRef);

                    activity.getDataInputAssociations().add(dia);
                });

        assignmentsInfo.getAssociations()
                .getOutputs()
                .stream()
                .map(this::addDataOutputAssociation)
                .forEach(doa -> {
                    OutputSet outputSet = bpmn2.createOutputSet();
                    doa.getSourceRef().forEach(this::addBaseElement);
                    this.addBaseElement(doa.getTargetRef());
                    ioSpec.getOutputSets().add(outputSet);

                    doa.getSourceRef().forEach(i -> {
                        DataOutput sourceRef = (DataOutput) i;
                        outputSet.getDataOutputRefs().add(sourceRef);
                        ioSpec.getDataOutputs().add(sourceRef);
                    });
                    activity.getDataOutputAssociations().add(doa);
                });

    }

    protected void setInput(String name, Object value) {
        activity.getDataInputAssociations().add(input(name, value));
    }

//    public void setInput(String name, Object value) {
//        DataInputAssociation association = bpmn2.createDataInputAssociation();
//
//        DataInput input = bpmn2.createDataInput();
//        input.setName(name);
//
//        Assignment assignment = bpmn2.createAssignment();
//        FormalExpression fromExpression = bpmn2.createFormalExpression();
////        fromExpression.setBody();
////        assignment.setFrom();
//
//        activity.getDataInputAssociations().add(association);
//    }

//    public Optional<String> optionalInput(String name) {
//        for (DataInputAssociation din : task.getDataInputAssociations()) {
//            DataInput targetRef = (DataInput) (din.getTargetRef());
//            if (targetRef.getName().equalsIgnoreCase(name)) {
//                Assignment assignment = din.getAssignment().get(0);
//                return Optional.of(evaluate(assignment).toString());
//            }
//        }
//        return Optional.empty();
//    }

    private static Object evaluate(Assignment assignment) {
        return ((FormalExpression) assignment.getFrom()).getBody();
    }
}
