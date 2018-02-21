package org.kie.workbench.common.stunner.bpmn.backend.converters;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

import org.kie.workbench.common.stunner.bpmn.definition.SequenceFlow;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public interface EdgeResult<T> {

    static <R> EdgeResult<R> of(Edge<? extends View<R>, ?> value) {
        return new Success<>(value);
    }

    static <R> EdgeResult<R> success(Edge<? extends View<R>, ?> value) {
        return new Success<>(value);
    }

    static <R> EdgeResult<R> failure(String reason) {
        return new Failure<>(reason);
    }

    static <U> EdgeResult<U> ignored(String reason) {
        return new Ignored<>(reason);
    }

    default Edge<? extends View<T>, ?> value() {
        return asSuccess().value();
    }

    default String getId() {
        return value().getUUID();
    }

    <U> EdgeResult<U> map(
            Function<? super Edge<? extends View<T>, ?>,
                    ? extends Edge<? extends View<U>, ?>> mapper);

    boolean isFailure();

    boolean isIgnored();

    boolean isSuccess();

    default boolean nonFailure() {
        return !isFailure();
    }

    default boolean notIgnored() {
        return !isIgnored();
    }

    default void ifSuccess(Consumer<Edge<? extends View<T>, ?>> consumer) {
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

    default void setParent(EdgeResult<?> firstDiagramNode) {
        asSuccess().setParent(firstDiagramNode);
    }

    default void addChild(Success<?> child) {
        asSuccess().addChild(child);
    }

    default void removeChild(Success<?> child) {
        asSuccess().removeChild(child);
    }

    default List<Success<?>> getChildren() {
        return asSuccess().getChildren();
    }

    class Success<T> implements EdgeResult<T> {

        private final Edge<? extends View<T>, ?> value;
        private List<Success<?>> children = new ArrayList<>();
        private EdgeResult<?> parent;

        Success(Edge<? extends View<T>, ?> value) {
            this.value = value;
        }

        @Override
        public void setParent(EdgeResult<?> parent) {
            if (this.parent != null) {
                this.parent.removeChild(this);
            }
            this.parent = parent;
            parent.addChild(this);
        }

        public EdgeResult<?> getParent() {
            return parent;
        }

        @Override
        public void addChild(Success<?> child) {
            this.children.add(child);
        }

        @Override
        public void removeChild(Success<?> child) {
            this.children.remove(child);
        }

        @Override
        public List<Success<?>> getChildren() {
            return children;
        }

        public Edge<? extends View<T>, ?> value() {
            return value;
        }

        public <U> EdgeResult<U> map(
                Function<? super Edge<? extends View<T>, ?>,
                        ? extends Edge<? extends View<U>, ?>> mapper) {
            return EdgeResult.of(mapper.apply(this.value()));
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

    class Ignored<T> implements EdgeResult<T> {

        private final String reason;

        Ignored(String reason) {
            this.reason = reason;
        }

        public String reason() {
            return reason;
        }

        public <U> EdgeResult<U> map(
                Function<? super Edge<? extends View<T>, ?>,
                        ? extends Edge<? extends View<U>, ?>> mapper) {
            return (Ignored<U>) this;
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

    class Failure<T> implements EdgeResult<T> {

        private final String reason;

        Failure(String reason) {
            this.reason = reason;
        }

        public String reason() {
            return reason;
        }

        public <U> EdgeResult<U> map(
                Function<? super Edge<? extends View<T>, ?>,
                        ? extends Edge<? extends View<U>, ?>> mapper) {
            return (Failure<U>) this;
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

