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

package org.kie.workbench.common.stunner.bpmn.definition.property.dataio;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DeclarationList {

    private final List<AssignmentDeclaration> declarations;

    public DeclarationList() {
        this.declarations = Collections.emptyList();
    }

    public DeclarationList(List<AssignmentDeclaration> declarations) {
        this.declarations = declarations;
    }

    public String lookup(String identifier) {
        return declarations.stream().filter(d -> identifier.equals(d.getIdentifier()))
                .findFirst().map(AssignmentDeclaration::getType).orElse(null);
    }

    @Override
    public String toString() {
        return declarations.stream()
                .map(AssignmentDeclaration::toString)
                .collect(Collectors.joining(","));
    }

    public static DeclarationList fromString(String encoded) {
        return new DeclarationList(
                Arrays.asList(encoded.split(",")).stream()
                        .map(AssignmentDeclaration::fromString)
                        .collect(Collectors.toList()));
    }
}
