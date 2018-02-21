package org.kie.workbench.common.stunner.bpmn.backend.converters;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import com.sun.org.apache.regexp.internal.RE;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagramImpl;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public interface NodeResult<T> {

    static <R> NodeResult<R> of(Node<? extends View<? extends R>, ?> value) {
        return new Success<>(value);
    }

    static <R> NodeResult<R> success(Node<? extends View<? extends R>, ?> value) {
        return new Success<>(value);
    }

    static <R> NodeResult<R> failure(String reason) {
        return new Failure<>(reason);
    }

    static <U> NodeResult<U> ignored(String reason) {
        return new Ignored<>(reason);
    }

    default Node<? extends View<? extends T>, ?> value() {
        return asSuccess().value();
    }

    default String getId() {
        return value().getUUID();
    }

    boolean isFailure();

    boolean isIgnored();

    boolean isSuccess();

    default boolean nonFailure() {
        return !isFailure();
    }

    default boolean notIgnored() {
        return !isIgnored();
    }

    default void ifSuccess(Consumer<Node<? extends View<? extends T>, ?>> consumer) {
        if (isSuccess()) {
            consumer.accept(asSuccess().value());
        }
    }

    default void ifFailure(Consumer<String> consumer) {
        if (isFailure()) {
            consumer.accept(asFailure().reason());
        }
    }

    Success<T> asSuccess();

    Failure<T> asFailure();

    default void setParent(NodeResult<?> firstDiagramNode) {
        asSuccess().setParent(firstDiagramNode);
    }

    default void addChild(Success<?> child) {
        asSuccess().addChild(child);
    }

    default List<Success<?>> getChildren() {
        return asSuccess().getChildren();
    }

    class Success<T> implements NodeResult<T> {

        private final Node<? extends View<? extends T>, ?> value;
        private List<Success<?>> children = new ArrayList<>();
        private NodeResult<?> parent;

        Success(Node<? extends View<? extends T>, ?> value) {
            this.value = value;
        }

        @Override
        public void setParent(NodeResult<?> parent) {
            this.parent = parent;
            parent.addChild(this);
        }

        public NodeResult<?> getParent() {
            return parent;
        }

        @Override
        public void addChild(Success<?> child) {
            this.children.add(child);
        }

        @Override
        public List<Success<?>> getChildren() {
            return children;
        }

        public Node<? extends View<? extends T>, ?> value() {
            return value;
        }

        public Success<T> asSuccess() {
            return this;
        }

        public Ignored<T> asIgnored() {
            throw new ClassCastException("Could not convert Success to Ignored");
        }

        public Failure<T> asFailure() {
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

    class Ignored<T> implements NodeResult<T> {

        private final String reason;

        Ignored(String reason) {
            this.reason = reason;
        }

        public String reason() {
            return reason;
        }

        public Success<T> asSuccess() {
            throw new NoSuchElementException(reason);
        }

        public Ignored<T> asIgnored() {
            return this;
        }

        public Failure<T> asFailure() {
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

    class Failure<T> implements NodeResult<T> {

        private final String reason;

        Failure(String reason) {
            this.reason = reason;
        }

        public String reason() {
            return reason;
        }

        public Success<T> asSuccess() {
            throw new NoSuchElementException(reason);
        }

        public Ignored<T> asIgnored() {
            throw new ClassCastException("Could not convert Failure to Ignored");
        }

        public Failure<T> asFailure() {
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

