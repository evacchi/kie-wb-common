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

package org.kie.workbench.common.stunner.bpmn.backend.converters;

import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Function;

import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public class BpmnMatchEdge<In, Out> {

    private final Class<?> outputType;
    LinkedList<BpmnMatchEdge.Case<?, Out>> cases = new LinkedList<>();
    Function<In, Edge<? extends View<Out>, ?>> orElse;

    public BpmnMatchEdge(Class<?> outputType) {
        this.outputType = outputType;
    }

    public static <In, Out> BpmnMatchEdge<In, Out> ofEdge(Class<In> inputType, Class<Out> outputType) {
        return new BpmnMatchEdge<>(outputType);
    }

    static <T, U> Function<T, EdgeResult<U>> reportMissing(Class<?> expectedClass) {
        return t ->
                EdgeResult.failure(
                        "Not yet implemented: " +
                                Optional.ofNullable(t)
                                        .map(o -> o.getClass().getCanonicalName())
                                        .orElse("null -- expected " + expectedClass.getCanonicalName()));
    }

    static <T, U> Function<T, EdgeResult<U>> ignored(Class<?> expectedClass) {
        return t ->
                EdgeResult.ignored(
                        "Ignored: " +
                                Optional.ofNullable(t)
                                        .map(o -> o.getClass().getCanonicalName())
                                        .orElse("null -- expected " + expectedClass.getCanonicalName()));
    }

    public <Sub> BpmnMatchEdge<In, Out> when(Class<Sub> type, Function<Sub, Edge<? extends View<Out>, ?>> then) {
        Function<Sub, EdgeResult<Out>> thenWrapped = sub -> EdgeResult.of(then.apply(sub));
        return when_(type, thenWrapped);
    }

    public <Sub> BpmnMatchEdge<In, Out> when_(Class<Sub> type, Function<Sub, EdgeResult<Out>> then) {
        cases.add(new BpmnMatchEdge.Case<>(type, then));
        return this;
    }

    /**
     * handle a type by throwing an error.
     * Use when the implementation is still missing, but expected to exist
     */
    public <Sub> BpmnMatchEdge<In, Out> missing(Class<Sub> type) {
        return when_(type, reportMissing(type));
    }

    public <Sub> BpmnMatchEdge<In, Out> ignore(Class<Sub> type) {
        return when_(type, ignored(type));
    }

    public BpmnMatchEdge<In, Out> orElse(Function<In, Edge<? extends View<Out>, ?>> then) {
        this.orElse = then;
        return this;
    }

    public EdgeResult<Out> apply(In value) {
        return cases.stream()
                .map(c -> c.match(value))
                .filter(EdgeResult::nonFailure)
                .findFirst()
                .orElse(applyFallback(value));
    }

    private EdgeResult<Out> applyFallback(In value) {
        if (orElse == null) {
            return EdgeResult.failure(value == null ? "Null" : value.getClass().getName());
        } else {
            return EdgeResult.of(orElse.apply(value));
        }
    }

    private static class Case<T, R> {

        public final Class<T> when;
        public final Function<T, EdgeResult<R>> then;

        public Case(Class<T> when, Function<T, EdgeResult<R>> then) {
            this.when = when;
            this.then = then;
        }

        public EdgeResult<R> match(Object value) {
            return when.isAssignableFrom(value.getClass()) ?
                    then.apply((T) value) : EdgeResult.failure(value.getClass().getName());
        }
    }
}
