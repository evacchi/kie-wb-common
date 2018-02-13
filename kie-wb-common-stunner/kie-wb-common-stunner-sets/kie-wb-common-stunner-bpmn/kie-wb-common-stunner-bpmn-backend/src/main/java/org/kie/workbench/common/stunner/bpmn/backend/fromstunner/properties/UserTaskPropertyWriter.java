package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import java.util.UUID;

import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.InputOutputSpecification;
import org.eclipse.bpmn2.InputSet;
import org.eclipse.bpmn2.PotentialOwner;
import org.eclipse.bpmn2.ResourceAssignmentExpression;
import org.eclipse.bpmn2.UserTask;
import org.kie.workbench.common.stunner.bpmn.definition.property.assignee.Actors;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;
import org.kie.workbench.common.stunner.bpmn.definition.property.simulation.SimulationSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.OnEntryAction;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.OnExitAction;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class UserTaskPropertyWriter extends ActivityPropertyWriter {

    private final UserTask task;

    public UserTaskPropertyWriter(UserTask task) {
        super(task);
        this.task = task;
    }

    public void setAsync(boolean async) {
        setMeta("customAsync", String.valueOf(async));
    }

    public void setSkippable(boolean skippable) {
        setInput("Skippable", String.valueOf(skippable));
    }

    public void setPriority(String priority) {
        setInput("Priority", priority);
    }

    public void setSubject(String subject) {
        setInput("Comment", subject);
    }

    public void setDescription(String description) {
        setInput("Description", description);
    }

    public void setCreatedBy(String createdBy) {
        setInput("CreatedBy", createdBy);
    }

    public void setAdHocAutostart(boolean autoStart) {
        setMeta("customAutoStart", String.valueOf(autoStart));
    }

    public void setAssignmentsInfo(AssignmentsInfo assignmentsInfo) {
        InputOutputSpecification ioSpec = bpmn2.createInputOutputSpecification();
        task.setIoSpecification(ioSpec);
        assignmentsInfo.getAssociations().getInputs()
                .stream()
                .map(this::addDataInputAssociation)
                .forEach(dia -> {
                    InputSet inputSet = bpmn2.createInputSet();
                    inputSet.getDataInputRefs().add((DataInput) dia.getTargetRef());
                    ioSpec.getInputSets().add(inputSet);
                    dia.getSourceRef().forEach(this::addBaseElement);
                    task.getDataInputAssociations().add(dia);
                });
    }

    public void setTaskName(String taskName) {

    }

    public void setActors(Actors actors) {
        for (String actor : actors.getValue().split(",")) {
            PotentialOwner potentialOwner = bpmn2.createPotentialOwner();
            potentialOwner.setId(UUID.randomUUID().toString());

            FormalExpression formalExpression = bpmn2.createFormalExpression();
            formalExpression.setBody(actor);

            ResourceAssignmentExpression resourceAssignmentExpression = bpmn2.createResourceAssignmentExpression();
            resourceAssignmentExpression.setExpression(formalExpression);

            potentialOwner.setResourceAssignmentExpression(resourceAssignmentExpression);

            task.getResources().add(potentialOwner);
        }
    }

    public void setGroupId(String value) {
        setInput("GroupId", value);
    }

    public void setOnEntryAction(OnEntryAction onEntryAction) {

    }

    public void setOnExitAction(OnExitAction onExitAction) {

    }

    public void setSimulationSet(SimulationSet simulationSet) {

    }
}
