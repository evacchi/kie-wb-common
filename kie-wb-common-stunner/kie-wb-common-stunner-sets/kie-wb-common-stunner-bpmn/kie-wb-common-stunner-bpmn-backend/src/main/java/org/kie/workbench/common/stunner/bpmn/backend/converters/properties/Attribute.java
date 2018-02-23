package org.kie.workbench.common.stunner.bpmn.backend.converters.properties;

import org.eclipse.bpmn2.BaseElement;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.Package;

public class Attribute<T> {

    public static final AttributeDefinition<Boolean> independent = new BooleanAttribute("independent", false);
    public static final AttributeDefinition<Boolean> adHoc = new BooleanAttribute("adHoc", false);
    public static final AttributeDefinition<Boolean> waitforCompletion = new BooleanAttribute("waitforCompletion", false);
    public static final AttributeDefinition<String> dockerInfo = new StringAttribute("dockerInfo", "");
    public static final AttributeDefinition<String> ruleFlowGroup = new StringAttribute("ruleFlowGroup", "");
    public static final AttributeDefinition<String> packageName = new StringAttribute("packageName", Package.DEFAULT_PACKAGE);
    public static final AttributeDefinition<String> version = new StringAttribute("version", "1.0");

    private final AttributeDefinition<T> attributeDefinition;
    private final BaseElement element;

    public Attribute(AttributeDefinition<T> attributeDefinition, BaseElement element) {
        this.attributeDefinition = attributeDefinition;
        this.element = element;
    }

    public T get() {
        return attributeDefinition.getValue(element);
    }

    public void set(T value) {
        attributeDefinition.setValue(element, value);
    }
}
