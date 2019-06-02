package org.kie.workbench.common.stunner.bpmn.backend;

import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public class ConverterTypes {

    public static <T> Node<View<T>, Edge> cast(Node<?, ?> node) {
        return (Node<View<T>, Edge>) node;
    }
}
