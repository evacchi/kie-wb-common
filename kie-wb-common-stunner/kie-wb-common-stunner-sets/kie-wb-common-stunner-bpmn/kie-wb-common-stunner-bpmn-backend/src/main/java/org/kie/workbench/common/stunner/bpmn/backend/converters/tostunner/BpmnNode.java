/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

/**
 * The result of a Converter (to Stunner) is always a BpmnNode.
 * It wraps the underlying Stunner node into a data structure
 * that also encodes
 * <p>
 * 1) parent/child relationships
 * 2) other edges (and therefore, implicitly, other nodes)
 * that may be contained inside the node (e.g. in the case of a (Sub)Process)
 */
public class BpmnNode {

    private final Node<? extends View<? extends BPMNViewDefinition>, ?> value;
    private final List<BpmnNode> children = new ArrayList<>();
    private List<BpmnEdge> edges = new ArrayList<>();
    private BpmnNode parent;

    protected BpmnNode(Node<? extends View<? extends BPMNViewDefinition>, ?> value) {
        this.value = value;
    }

    public static BpmnNode of(Node<? extends View<? extends BPMNViewDefinition>, ?> value) {
        return new BpmnNode(value);
    }

    public BpmnNode getParent() {
        return parent;
    }

    public void setParent(BpmnNode parent) {
        System.out.println(parent.value().getUUID() + " -> " + this.value.getUUID());
        if (this.parent != null) {
            this.parent.removeChild(this);
        }
        this.parent = parent;
        parent.addChild(this);
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

    public void addAllEdges(Collection<BpmnEdge> bpmnEdges) {
        this.edges.addAll(bpmnEdges);
    }

    public List<BpmnEdge> getEdges() {
        return edges;
    }

    public void addEdge(BpmnEdge bpmnEdge) {
        edges.add(bpmnEdge);
    }
}
