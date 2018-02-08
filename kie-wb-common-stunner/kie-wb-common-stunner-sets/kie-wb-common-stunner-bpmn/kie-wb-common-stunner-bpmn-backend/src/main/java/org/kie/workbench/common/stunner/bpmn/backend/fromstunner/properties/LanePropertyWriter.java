package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import org.eclipse.bpmn2.Lane;

public class LanePropertyWriter extends BasePropertyWriter {

    public LanePropertyWriter(Lane lane) {
        super(lane);
    }

    @Override
    public Lane getElement() {
        return (Lane) super.getElement();
    }
}
