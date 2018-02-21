package org.kie.workbench.common.stunner.bpmn.backend.converters;

import java.util.ArrayList;
import java.util.List;

import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public class BpmnNode {

    public static BpmnNode of(Node<? extends View<? extends BPMNViewDefinition>, ?> value) {
        return new BpmnNode(value);
    }

    private final Node<? extends View<? extends BPMNViewDefinition>, ?> value;
    private final List<BpmnNode> children = new ArrayList<>();
    private BpmnNode parent;

    private BpmnNode(Node<? extends View<? extends BPMNViewDefinition>, ?> value) {
        this.value = value;
    }

    public void setParent(BpmnNode parent) {
        if (this.parent != null) {
            this.parent.removeChild(this);
        }
        this.parent = parent;
        parent.addChild(this);
    }

    public BpmnNode getParent() {
        return parent;
    }

    public void addChild(BpmnNode child) {
        this.children.add(child);
    }

    public void removeChild(BpmnNode child) {
        this.children.remove(child);
    }

    public List<BpmnNode> getChildren() {
        return children;
    }

    public Node<? extends View<? extends BPMNViewDefinition>, ?> value() {
        return value;
    }
}
