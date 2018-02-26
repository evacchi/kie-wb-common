package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import bpsim.ElementParameters;
import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.InputOutputSpecification;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.Simulations;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssociationDeclaration;
import org.kie.workbench.common.stunner.bpmn.definition.property.simulation.SimulationSet;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class ActivityPropertyWriter extends PropertyWriter {

    protected final Activity activity;
    private ElementParameters simulationParameters;

    public ActivityPropertyWriter(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    public Activity getFlowElement() {
        return activity;
    }

    public void setSimulationSet(SimulationSet simulationSet) {
        this.simulationParameters = Simulations.toElementParameters(simulationSet);
        simulationParameters.setElementRef(activity.getId());
    }

    public ElementParameters getSimulationParameters() {
        return simulationParameters;
    }

    public void setAssignmentsInfo(AssignmentsInfo assignmentsInfo) {
        final InputOutputSpecification ioSpec = getIoSpecification();

        assignmentsInfo.getAssociations()
                .getInputs()
                .stream()
                .map(declaration -> new InputAssignmentWriter(
                        flowElement.getId(),
                        declaration,
                        assignmentsInfo
                                .getInputs()
                                .lookup(declaration.getRight())))
                .forEach(dia -> {
                    this.addBaseElement(dia.getItemDefinition());
                    this.addBaseElement(dia.getProperty());
                    this.addBaseElement(dia.getDataInput());
                    ioSpec.getInputSets().add(dia.getInputSet());
                    ioSpec.getDataInputs().add(dia.getDataInput());
                    activity.getDataInputAssociations().add(dia.getAssociation());
                });

        assignmentsInfo.getAssociations()
                .getOutputs()
                .stream()
                .map(declaration -> new OutputAssignmentWriter(
                        flowElement.getId(),
                        declaration,
                        assignmentsInfo
                                .getOutputs()
                                .lookup(declaration.getLeft())))
                .forEach(doa -> {
                    this.addBaseElement(doa.getItemDefinition());
                    this.addBaseElement(doa.getProperty());
                    this.addBaseElement(doa.getDataOutput());
                    ioSpec.getOutputSets().add(doa.getOutputSet());
                    ioSpec.getDataOutputs().add(doa.getDataOutput());
                    activity.getDataOutputAssociations().add(doa.getAssociation());
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

//    protected void setInput(String name, String value) {
//        if (value == null || value.isEmpty()) {
//            return;
//        }
//        DataInputAssociation input = input(name, value);
//        getIoSpecification().getDataInputs().add((DataInput) input.getTargetRef());
//        activity.getDataInputAssociations().add(input);
//    }
}
