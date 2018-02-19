package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.DataOutput;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.InputOutputSpecification;
import org.eclipse.bpmn2.InputSet;
import org.eclipse.bpmn2.OutputSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class ActivityPropertyWriter extends IOPropertyWriter {

    protected final Activity activity;

    public ActivityPropertyWriter(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    public Activity getFlowElement() {
        return activity;
    }

    public void setAssignmentsInfo(AssignmentsInfo assignmentsInfo) {
        final InputOutputSpecification ioSpec = getIoSpecification();

        assignmentsInfo.getAssociations()
                .getInputs()
                .stream()
                .map(declaration -> addDataInputAssociation(declaration, assignmentsInfo.getInputs()))
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
                .map(declaration -> this.addDataOutputAssociation(declaration, assignmentsInfo.getOutputs()))
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

    private InputOutputSpecification getIoSpecification() {
        InputOutputSpecification ioSpecification = activity.getIoSpecification();
        if (ioSpecification == null) {
            ioSpecification = bpmn2.createInputOutputSpecification();
            activity.setIoSpecification(ioSpecification);
        }
        return ioSpecification;
    }

    protected void setInput(String name, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        DataInputAssociation input = input(name, value);
        getIoSpecification().getDataInputs().add((DataInput) input.getTargetRef());
        activity.getDataInputAssociations().add(input);
    }
}
