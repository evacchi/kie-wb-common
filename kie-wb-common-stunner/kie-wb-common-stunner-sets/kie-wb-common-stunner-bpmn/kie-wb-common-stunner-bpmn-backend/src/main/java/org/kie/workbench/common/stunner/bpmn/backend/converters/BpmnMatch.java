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

import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

public class BpmnMatch<In> {

    LinkedList<BpmnMatch.Case<?, BPMNViewDefinition>> cases = new LinkedList<>();
    Function<? super In, ? extends Node<? extends View<? extends BPMNViewDefinition>, ?>> orElse;

    public BpmnMatch() {
    }

    public static <In, Out> BpmnMatch<In> of(Class<In> inputType) {
        return new BpmnMatch<>();
    }

    public static <In, Out> BpmnMatch<In> ofNode(Class<In> inputType, Class<Out> outputType) {
        return new BpmnMatch<>();
    }

    public static <In, Out> BpmnMatch<In> ofEdge(Class<In> inputType, Class<Out> outputType) {
        return new BpmnMatch<>();
    }

    static <T, U> Function<T, NodeResult> reportMissing(Class<?> expectedClass) {
        return t ->
                NodeResult.failure(
                        "Not yet implemented: " +
                                Optional.ofNullable(t)
                                        .map(o -> o.getClass().getCanonicalName())
                                        .orElse("null -- expected " + expectedClass.getCanonicalName()));
    }

    static <T, U> Function<T, NodeResult> ignored(Class<?> expectedClass) {
        return t ->
                NodeResult.ignored(
                        "Ignored: " +
                                Optional.ofNullable(t)
                                        .map(o -> o.getClass().getCanonicalName())
                                        .orElse("null -- expected " + expectedClass.getCanonicalName()));
    }

    public <Sub> BpmnMatch<In> when(Class<Sub> type, Function<Sub, NodeResult> then) {
        cases.add(new BpmnMatch.Case<>(type, then));
        return this;
    }

    /**
     * handle a type by throwing an error.
     * Use when the implementation is still missing, but expected to exist
     */
    public <Sub> BpmnMatch<In> missing(Class<Sub> type) {
        return when(type, reportMissing(type));
    }

    public <Sub> BpmnMatch<In> ignore(Class<Sub> type) {
        return when(type, ignored(type));
    }

    public BpmnMatch<In> orElse(Function<In, Node<? extends View<? extends BPMNViewDefinition>, ?>> then) {
        this.orElse = then;
        return this;
    }

    public NodeResult apply(In value) {
        return cases.stream()
                .map(c -> c.match(value))
                .filter(NodeResult::nonFailure)
                .findFirst()
                .orElse(applyFallback(value));
    }

    private NodeResult applyFallback(In value) {
        if (orElse == null) {
            return NodeResult.failure(value == null ? "Null" : value.getClass().getName());
        } else {
            return NodeResult.of(orElse.apply(value));
        }
    }

    private static class Case<T, R> {

        public final Class<T> when;
        public final Function<T, NodeResult> then;

        public Case(Class<T> when, Function<T, NodeResult> then) {
            this.when = when;
            this.then = then;
        }

        public NodeResult match(Object value) {
            return when.isAssignableFrom(value.getClass()) ?
                    then.apply((T) value) : NodeResult.failure(value.getClass().getName());
        }
    }
}
