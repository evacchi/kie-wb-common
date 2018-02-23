package org.kie.workbench.common.stunner.bpmn.backend.converters.properties;

import java.util.Optional;

import org.eclipse.bpmn2.BaseElement;
import org.kie.workbench.common.stunner.bpmn.definition.property.diagram.Package;
import org.kie.workbench.common.stunner.core.graph.content.view.Point2D;

public class Attribute<T> {

    public static final AttributeDefinition<Boolean> independent = new BooleanAttribute("independent", false);
    public static final AttributeDefinition<Boolean> adHoc = new BooleanAttribute("adHoc", false);
    public static final AttributeDefinition<Boolean> waitForCompletion = new BooleanAttribute("waitForCompletion", false);
    public static final AttributeDefinition<String> ruleFlowGroup = new StringAttribute("ruleFlowGroup", "");
    public static final AttributeDefinition<String> packageName = new StringAttribute("packageName", Package.DEFAULT_PACKAGE);
    public static final AttributeDefinition<String> version = new StringAttribute("version", "1.0");
    public static final AttributeDefinition<Boolean> boundarycaForBoundaryEvent = new BooleanAttribute("boundaryca", true);
    public static final AttributeDefinition<Boolean> boundarycaForEvent = new BooleanAttribute("boundaryca", false);
    public static final AttributeDefinition<String> priority = new StringAttribute("priority", null);
    public static final AttributeDefinition<String> dtype = new StringAttribute("dtype", "");
    public static final AttributeDefinition<String> dg = new StringAttribute("dg", "") {
        @Override
        public String getValue(BaseElement element) {
            // this is for compatibility with legacy marshallers
            // always return null regardless the string was empty in the file
            // or it was actually undefined
            String value = super.getValue(element);
            return value.isEmpty() ? null : value;
        }
    };
    public static final AttributeDefinition<Point2D> dockerInfo = new AttributeDefinition<Point2D>("dockerinfo", Point2D.create(0, 0)) {
        @Override
        public Point2D getValue(BaseElement element) {
            Optional<String> attribute = getStringValue(element);

            if (attribute.isPresent()) {
                String dockerInfoStr = attribute.get();
                dockerInfoStr = dockerInfoStr.substring(0, dockerInfoStr.length() - 1);
                String[] dockerInfoParts = dockerInfoStr.split("\\|");
                String infoPartsToUse = dockerInfoParts[0];
                String[] infoPartsToUseParts = infoPartsToUse.split("\\^");

                double x = Double.valueOf(infoPartsToUseParts[0]);
                double y = Double.valueOf(infoPartsToUseParts[1]);

                return Point2D.create(x, y);
            } else {
                return Point2D.create(0, 0);
            }
        }

        @Override
        public void setValue(BaseElement element, Point2D value) {
            throw new UnsupportedOperationException("not yet implemented");
        }
    };

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
