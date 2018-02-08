package org.kie.workbench.common.stunner.bpmn.backend.fromstunner;

import org.eclipse.bpmn2.di.BPMNEdge;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.BasePropertyWriter;

public interface ElementContainer {

    BasePropertyWriter getChildElement(String uuid);

    void addChildElement(BasePropertyWriter p);

    void addChildEdge(BPMNEdge edge);
}
