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

package org.kie.workbench.common.stunner.bpmn.backend.converters.customproperties;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class DeclarationList {

    private final List<VariableDeclaration> declarations;

    public DeclarationList() {
        this.declarations = Collections.emptyList();
    }

    public DeclarationList(List<VariableDeclaration> declarations) {
        this.declarations = declarations;
    }

    public DeclarationList(Stream<VariableDeclaration> declarations) {
        this.declarations = declarations.collect(toList());
    }

    public static DeclarationList fromString(String encoded) {
        return new DeclarationList(
                Arrays.stream(encoded.split(","))
                        .filter(s -> !s.isEmpty()) // "" makes no sense
                        .map(VariableDeclaration::fromString)
                        .collect(toList()));
    }

    public VariableDeclaration lookup(String identifier) {
        return declarations.stream().filter(d -> identifier.equals(d.getIdentifier()))
                .findFirst().orElse(null);
    }

    public Collection<VariableDeclaration> getDeclarations() {
        return declarations;
    }

    @Override
    public String toString() {
        return asString();
    }

    public String asString() {
        return declarations.stream()
                .map(VariableDeclaration::toString)
                .sorted(String::compareTo)
                .collect(Collectors.joining(","));
    }
}
