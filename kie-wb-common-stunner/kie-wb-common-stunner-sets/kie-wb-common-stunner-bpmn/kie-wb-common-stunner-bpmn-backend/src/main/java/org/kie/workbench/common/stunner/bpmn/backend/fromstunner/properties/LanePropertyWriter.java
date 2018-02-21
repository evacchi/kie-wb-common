package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Lane;

public class LanePropertyWriter extends BasePropertyWriter {

    private final Lane lane;

    public LanePropertyWriter(Lane lane) {
        super(lane);
        this.lane = lane;
    }

    @Override
    public Lane getElement() {
        return (Lane) super.getElement();
    }

    @Override
    public void addChild(BasePropertyWriter child) {
        lane.getFlowNodeRefs().add((FlowNode) child.getElement());
    }
}
