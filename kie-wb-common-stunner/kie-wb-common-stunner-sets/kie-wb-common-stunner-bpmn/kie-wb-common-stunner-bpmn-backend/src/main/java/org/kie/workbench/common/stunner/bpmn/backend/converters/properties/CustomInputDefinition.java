package org.kie.workbench.common.stunner.bpmn.backend.converters.properties;

import java.util.List;
import java.util.Optional;

import org.eclipse.bpmn2.Assignment;
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.Task;

public abstract class CustomInputDefinition<T> {

    private final String name;
    protected final T defaultValue;

    public CustomInputDefinition(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String name() {
        return name;
    }

    public abstract T getValue(Task element);

    public abstract void setValue(Task element, T value);

    Optional<String> getStringValue(Task element) {
        for (DataInputAssociation din : element.getDataInputAssociations()) {
            DataInput targetRef = (DataInput) (din.getTargetRef());
            if (targetRef.getName().equalsIgnoreCase(name)) {
                Assignment assignment = din.getAssignment().get(0);
                return Optional.of(evaluate(assignment).toString());
            }
        }
        return Optional.empty();
    }

    private static Object evaluate(Assignment assignment) {
        return ((FormalExpression) assignment.getFrom()).getBody();
    }

    void setStringValue(Task element, String value) {
    }

    public CustomInput<T> of(Task element) {
        return new CustomInput<>(this, element);
    }
}

class BooleanInput extends CustomInputDefinition<Boolean> {

    BooleanInput(String name, Boolean defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public Boolean getValue(Task element) {
        return getStringValue(element)
                .map(Boolean::parseBoolean)
                .orElse(defaultValue);
    }

    @Override
    public void setValue(Task element, Boolean value) {
        setStringValue(element, String.valueOf(value));
    }
}

class StringInput extends CustomInputDefinition<String> {

    StringInput(String name, String defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public String getValue(Task element) {
        return getStringValue(element)
                .orElse(defaultValue);
    }

    @Override
    public void setValue(Task element, String value) {
        setStringValue(element, value);
    }
}