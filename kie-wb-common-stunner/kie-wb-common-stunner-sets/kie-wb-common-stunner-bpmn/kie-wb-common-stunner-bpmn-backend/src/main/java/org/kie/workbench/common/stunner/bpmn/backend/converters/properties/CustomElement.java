package org.kie.workbench.common.stunner.bpmn.backend.converters.properties;

import org.eclipse.bpmn2.BaseElement;

public class CustomElement<T> {

    public static final ElementDefinition<Boolean> async = new BooleanElement("customAsync", false);
    public static final ElementDefinition<Boolean> autoStart = new BooleanElement("customAutoStart", false);
    public static final ElementDefinition<Boolean> autoConnectionSource = new BooleanElement("isAutoConnection.source", false);
    public static final ElementDefinition<Boolean> autoConnectionTarget = new BooleanElement("isAutoConnection.target", false);
    public static final ElementDefinition<String> description = new StringElement("customDescription", "");
    public static final ElementDefinition<String> scope = new StringElement("customScope", "");


    private final ElementDefinition<T> elementDefinition;
    private final BaseElement element;

    public CustomElement(ElementDefinition<T> elementDefinition, BaseElement element) {
        this.elementDefinition = elementDefinition;
        this.element = element;
    }

    public T get() {
        return elementDefinition.getValue(element);
    }

    public void set(T value) {
        elementDefinition.setValue(element, value);
    }

}
