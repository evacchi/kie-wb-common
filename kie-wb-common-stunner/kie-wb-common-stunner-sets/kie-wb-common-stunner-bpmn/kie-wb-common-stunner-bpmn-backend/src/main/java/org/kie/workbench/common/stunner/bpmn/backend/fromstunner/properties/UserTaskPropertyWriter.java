package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import java.util.UUID;

import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.PotentialOwner;
import org.eclipse.bpmn2.ResourceAssignmentExpression;
import org.eclipse.bpmn2.UserTask;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.CustomElement;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.CustomInput;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.Scripts;
import org.kie.workbench.common.stunner.bpmn.definition.property.assignee.Actors;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.OnEntryAction;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.OnExitAction;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public class UserTaskPropertyWriter extends ActivityPropertyWriter {

    private final UserTask task;
    private final CustomInput<String> description;
    private final CustomInput<String> createdBy;
    private final CustomInput<String> taskName;
    private final CustomInput<String> groupId;
    private final CustomInput<Boolean> skippable;
    private final CustomInput<String> priority;
    private final CustomInput<String> subject;

    public UserTaskPropertyWriter(UserTask task, VariableScope variableScope) {
        super(task, variableScope);
        this.task = task;

        this.skippable = CustomInput.skippable.of(task);
        this.addBaseElement(this.skippable.typeDef());

        this.priority = CustomInput.priority.of(task);
        this.addBaseElement(this.priority.typeDef());

        this.subject = CustomInput.subject.of(task);
        this.addBaseElement(this.subject.typeDef());

        this.description = CustomInput.description.of(task);
        this.addBaseElement(this.description.typeDef());

        this.createdBy = CustomInput.createdBy.of(task);
        this.addBaseElement(this.createdBy.typeDef());

        this.taskName = CustomInput.taskName.of(task);
        this.addBaseElement(this.taskName.typeDef());

        this.groupId = CustomInput.groupId.of(task);
        this.addBaseElement(this.skippable.typeDef());
    }

    public void setAsync(boolean async) {
        CustomElement.async.of(task).set(async);
    }

    public void setSkippable(boolean skippable) {
        this.skippable.set(skippable);
    }

    public void setPriority(String priority) {
        this.priority.set(priority);
    }

    public void setSubject(String subject) {
        this.subject.set(subject);
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy.set(createdBy);
    }

    public void setAdHocAutostart(boolean autoStart) {
        CustomElement.autoStart.of(task).set(autoStart);
    }

    public void setTaskName(String taskName) {
        this.taskName.set(taskName);
    }

    public void setActors(Actors actors) {
        for (String actor : actors.getActors()) {
            PotentialOwner potentialOwner = bpmn2.createPotentialOwner();
            potentialOwner.setId(UUID.randomUUID().toString());

            FormalExpression formalExpression = bpmn2.createFormalExpression();
            formalExpression.setBody(actor);

            ResourceAssignmentExpression resourceAssignmentExpression =
                    bpmn2.createResourceAssignmentExpression();
            resourceAssignmentExpression.setExpression(formalExpression);

            potentialOwner.setResourceAssignmentExpression(resourceAssignmentExpression);

            task.getResources().add(potentialOwner);
        }
    }

    public void setGroupId(String value) {
        groupId.set(asCData(value));
    }

    public void setOnEntryAction(OnEntryAction onEntryAction) {
        Scripts.setOnEntryAction(task, onEntryAction);
    }

    public void setOnExitAction(OnExitAction onExitAction) {
        Scripts.setOnExitAction(task, onExitAction);
    }
}
