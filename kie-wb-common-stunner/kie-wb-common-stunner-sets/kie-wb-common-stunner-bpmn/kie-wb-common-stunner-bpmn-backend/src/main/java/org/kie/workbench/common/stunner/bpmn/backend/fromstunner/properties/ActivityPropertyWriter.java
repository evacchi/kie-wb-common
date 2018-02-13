package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.Assignment;
import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.InputOutputSpecification;
import org.eclipse.bpmn2.InputSet;
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

        /*
            <bpmn2:assignment id="_mK5jeH_mEeaT2qmUthBrDw">
        <bpmn2:from xsi:type="bpmn2:tFormalExpression" id="_mK5jeX_mEeaT2qmUthBrDw"><![CDATA[admin,kiemgmt]]></bpmn2:from>
        <bpmn2:to xsi:type="bpmn2:tFormalExpression" id="_mK5jen_mEeaT2qmUthBrDw">_18189082-D105-4BAB-B62C-34C5F8AF5D5B_GroupIdInputX</bpmn2:to>
    </bpmn2:assignment>

         */

        assignmentsInfo.getAssociations()
                .getInputs()
                .stream()
                .map(this::addDataInputAssociation)
                .forEach(dia -> {
                    InputSet inputSet = bpmn2.createInputSet();
                    inputSet.getDataInputRefs().add((DataInput) dia.getTargetRef());
                    ioSpec.getInputSets().add(inputSet);
                    dia.getSourceRef().forEach(this::addBaseElement);
                    ioSpec.getDataInputs().add((DataInput) dia.getTargetRef());
                    activity.getDataInputAssociations().add(dia);
                });
    }

    public void setInput(String name, String value) {
        DataInputAssociation association = bpmn2.createDataInputAssociation();

        DataInput input = bpmn2.createDataInput();
        input.setName(name);

        Assignment assignment = bpmn2.createAssignment();
        FormalExpression fromExpression = bpmn2.createFormalExpression();
//        fromExpression.setBody();
//        assignment.setFrom();

        activity.getDataInputAssociations().add(association);
    }

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
