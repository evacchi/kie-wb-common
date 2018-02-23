package org.kie.workbench.common.stunner.bpmn.backend.converters.properties;

import java.util.Optional;

import org.eclipse.bpmn2.BaseElement;

public abstract class AttributeDefinition<T> {

    private final String name;
    protected final T defaultValue;
    private T value;

    public AttributeDefinition(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String name() {
        return name;
    }

    public abstract T getValue(BaseElement element);

    public Optional<java.lang.String> getStringValue(BaseElement element) {
        return element.getAnyAttribute().stream()
                .filter(e -> this.name().equals(e.getEStructuralFeature().getName()))
                .map(e -> e.getValue().toString())
                .findFirst();
    }

    public Attribute<T> of(BaseElement element) {
        return new Attribute<>(this, element);
    }
}

class BooleanAttribute extends AttributeDefinition<Boolean> {

    public BooleanAttribute(String name, java.lang.Boolean defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public java.lang.Boolean getValue(BaseElement element) {
        return getStringValue(element)
                .map(java.lang.Boolean::parseBoolean)
                .orElse(defaultValue);
    }
}

class StringAttribute extends AttributeDefinition<String> {

    public StringAttribute(String name, java.lang.String defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public java.lang.String getValue(BaseElement element) {
        return getStringValue(element)
                .orElse(defaultValue);
    }
}