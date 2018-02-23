package org.kie.workbench.common.stunner.bpmn.backend.converters.properties;

import org.eclipse.bpmn2.Task;

public class CustomInput<T> {

    public static final CustomInputDefinition<String> taskName = new StringInput("TaskName", "Task");
    public static final CustomInputDefinition<String> priority = new StringInput("Priority", "");
    public static final CustomInputDefinition<String> subject = new StringInput("Comment", "");
    public static final CustomInputDefinition<String> description = new StringInput("Description", "");
    public static final CustomInputDefinition<String> createdBy = new StringInput("CreatedBy", "");
    public static final CustomInputDefinition<String> groupId = new StringInput("GroupId", "");
    public static final CustomInputDefinition<Boolean> skippable = new BooleanInput("Skippable", false);


    private final CustomInputDefinition<T> inputDefinition;
    private final Task element;

    public CustomInput(CustomInputDefinition<T> inputDefinition, Task element) {
        this.inputDefinition = inputDefinition;
        this.element = element;
    }

    public T get() {
        return inputDefinition.getValue(element);
    }

    public void set(T value) {
        inputDefinition.setValue(element, value);
    }
}
