package org.kie.workbench.common.stunner.bpmn.backend.converters.properties;

import java.util.List;
import java.util.Optional;

import org.eclipse.bpmn2.Assignment;
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.InputOutputSpecification;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.Property;
import org.eclipse.bpmn2.Task;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

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

}