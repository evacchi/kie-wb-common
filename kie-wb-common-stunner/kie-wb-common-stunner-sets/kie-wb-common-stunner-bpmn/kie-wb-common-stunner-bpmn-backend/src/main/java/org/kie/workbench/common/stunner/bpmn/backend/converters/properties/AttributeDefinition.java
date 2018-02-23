package org.kie.workbench.common.stunner.bpmn.backend.converters.properties;

import java.util.Optional;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.emf.ecore.impl.EAttributeImpl;
import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.metaData;

public abstract class AttributeDefinition<T> {

    private final String name;
    protected final T defaultValue;

    public AttributeDefinition(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String name() {
        return name;
    }

    public abstract T getValue(BaseElement element);

    public abstract void setValue(BaseElement element, T value);

    Optional<java.lang.String> getStringValue(BaseElement element) {
        return element.getAnyAttribute().stream()
                .filter(e -> this.name().equals(e.getEStructuralFeature().getName()))
                .map(e -> e.getValue().toString())
                .findFirst();
    }

    void setStringValue(BaseElement element, String value) {
        EAttributeImpl extensionAttribute = (EAttributeImpl) metaData.demandFeature(
                "http://www.jboss.org/drools",
                name,
                false,
                false);

        EStructuralFeatureImpl.SimpleFeatureMapEntry feature =
                new EStructuralFeatureImpl.SimpleFeatureMapEntry(extensionAttribute, value);

        element.getAnyAttribute().add(feature);
    }

    public Attribute<T> of(BaseElement element) {
        return new Attribute<>(this, element);
    }
}

class BooleanAttribute extends AttributeDefinition<Boolean> {

    BooleanAttribute(String name, java.lang.Boolean defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public java.lang.Boolean getValue(BaseElement element) {
        return getStringValue(element)
                .map(java.lang.Boolean::parseBoolean)
                .orElse(defaultValue);
    }

    @Override
    public void setValue(BaseElement element, Boolean value) {
        setStringValue(element, String.valueOf(value));
    }
}

class StringAttribute extends AttributeDefinition<String> {

    StringAttribute(String name, java.lang.String defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public java.lang.String getValue(BaseElement element) {
        return getStringValue(element)
                .orElse(defaultValue);
    }

    @Override
    public void setValue(BaseElement element, String value) {
        setStringValue(element, value);
    }
}