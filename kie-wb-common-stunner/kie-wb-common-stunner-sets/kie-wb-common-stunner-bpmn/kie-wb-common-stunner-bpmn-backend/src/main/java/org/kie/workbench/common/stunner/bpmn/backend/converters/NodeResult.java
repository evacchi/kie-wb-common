package org.kie.workbench.common.stunner.bpmn.backend.converters;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public interface NodeResult {

    static NodeResult of(Node<? extends View<? extends BPMNViewDefinition>, ?> value) {
        return new Success(value);
    }

    static NodeResult success(Node<? extends View<? extends BPMNViewDefinition>, ?> value) {
        return new Success(value);
    }

    static NodeResult failure(String reason) {
        return new Failure(reason);
    }

    static NodeResult ignored(String reason) {
        return new Ignored(reason);
    }

    default Node<? extends View<? extends BPMNViewDefinition>, ?> value() {
        return asSuccess().value();
    }

    default String getId() {
        return value().getUUID();
    }

    public NodeResult map(
            Function<? super Node<? extends View<? extends BPMNViewDefinition>, ?>,
                    ? extends Node<? extends View<? extends BPMNViewDefinition>, ?>> mapper);

    boolean isFailure();

    boolean isIgnored();

    boolean isSuccess();

    default boolean nonFailure() {
        return !isFailure();
    }

    default boolean notIgnored() {
        return !isIgnored();
    }

    default void ifSuccess(Consumer<Node<? extends View<? extends BPMNViewDefinition>, ?>> consumer) {
        if (isSuccess()) {
            consumer.accept(asSuccess().value());
        }
    }

    default void ifFailure(Consumer<String> consumer) {
        if (isFailure()) {
            consumer.accept(asFailure().reason());
        }
    }

    Success asSuccess();

    Failure asFailure();

    default void setParent(NodeResult firstDiagramNode) {
        asSuccess().setParent(firstDiagramNode);
    }

    default void addChild(Success child) {
        asSuccess().addChild(child);
    }

    default void removeChild(Success child) {
        asSuccess().removeChild(child);
    }

    default List<Success> getChildren() {
        return asSuccess().getChildren();
    }

    class Success implements NodeResult {

        private final Node<? extends View<? extends BPMNViewDefinition>, ?> value;
        private List<Success> children = new ArrayList<>();
        private NodeResult parent;

        Success(Node<? extends View<? extends BPMNViewDefinition>, ?> value) {
            this.value = value;
        }

        @Override
        public void setParent(NodeResult parent) {
            if (this.parent != null) {
                this.parent.removeChild(this);
            }
            this.parent = parent;
            parent.addChild(this);
        }

        public NodeResult getParent() {
            return parent;
        }

        @Override
        public void addChild(Success child) {
            this.children.add(child);
        }

        @Override
        public void removeChild(Success child) {
            this.children.remove(child);
        }

        @Override
        public List<Success> getChildren() {
            return children;
        }

        public Node<? extends View<? extends BPMNViewDefinition>, ?> value() {
            return value;
        }

        public NodeResult map(
                Function<? super Node<? extends View<? extends BPMNViewDefinition>, ?>,
                        ? extends Node<? extends View<? extends BPMNViewDefinition>, ?>> mapper) {
            return NodeResult.of(mapper.apply(this.value()));
        }

        public Success asSuccess() {
            return this;
        }

        public Ignored asIgnored() {
            throw new ClassCastException("Could not convert Success to Ignored");
        }

        public Failure asFailure() {
            throw new ClassCastException("Could not convert Success to Failure");
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        public boolean isIgnored() {
            return false;
        }

        public boolean isFailure() {
            return false;
        }
    }

    class Ignored implements NodeResult {

        private final String reason;

        Ignored(String reason) {
            this.reason = reason;
        }

        public String reason() {
            return reason;
        }

        public NodeResult map(
                Function<? super Node<? extends View<? extends BPMNViewDefinition>, ?>,
                        ? extends Node<? extends View<? extends BPMNViewDefinition>, ?>> mapper) {
            return this;
        }

        public Success asSuccess() {
            throw new NoSuchElementException(reason);
        }

        public Ignored asIgnored() {
            return this;
        }

        public Failure asFailure() {
            throw new ClassCastException("Could not convert Ignored to Success");
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        public boolean isIgnored() {
            return true;
        }

        public boolean isFailure() {
            return false;
        }
    }

    class Failure implements NodeResult {

        private final String reason;

        Failure(String reason) {
            this.reason = reason;
        }

        public String reason() {
            return reason;
        }

        public NodeResult map(
                Function<? super Node<? extends View<? extends BPMNViewDefinition>, ?>,
                        ? extends Node<? extends View<? extends BPMNViewDefinition>, ?>> mapper) {
            return this;
        }

        public Success asSuccess() {
            throw new NoSuchElementException(reason);
        }

        public Ignored asIgnored() {
            throw new ClassCastException("Could not convert Failure to Ignored");
        }

        public Failure asFailure() {
            return this;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        public boolean isIgnored() {
            return false;
        }

        public boolean isFailure() {
            return true;
        }
    }
}

