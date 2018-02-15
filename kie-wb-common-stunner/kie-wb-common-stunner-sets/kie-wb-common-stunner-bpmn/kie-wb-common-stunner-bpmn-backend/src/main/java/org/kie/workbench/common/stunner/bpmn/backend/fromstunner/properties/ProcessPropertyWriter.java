package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Process;

public class ProcessPropertyWriter {

    private final Process process;
    private List<BaseElement> baseElements = new ArrayList<>();

    public ProcessPropertyWriter(Process rootLevelProcess) {
        this.process = rootLevelProcess;
    }

    public Process getProcess() {
        return process;
    }

    public void addFlowElement(FlowElement flowElement) {
        process.getFlowElements().add(flowElement);
    }

    public void addAllBaseElements(Collection<BaseElement> baseElements) {
        this.baseElements.addAll(baseElements);
    }

    public List<BaseElement> getBaseElements() {
        return baseElements;
    }
}
