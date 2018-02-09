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

public abstract class AssociationDeclaration {

    private final String source;
    private final String target;

    public AssociationDeclaration(String source, String target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public String toString() {
        return String.format("%s->%s", source, target);
    }

    public static AssociationDeclaration ofInput(String source, String target) {
        return new InputAssociationDeclaration(source, target);
    }
    public static AssociationDeclaration ofOutput(String source, String target) {
        return new OutputAssociationDeclaration(source, target);
    }
}

class InputAssociationDeclaration extends AssociationDeclaration {

    public InputAssociationDeclaration(String source, String target) {
        super(source, target);
    }

    @Override
    public String toString() {
        return "[din]" + super.toString();
    }
}

class OutputAssociationDeclaration extends AssociationDeclaration {

    public OutputAssociationDeclaration(String source, String target) {
        super(source, target);
    }

    @Override
    public String toString() {
        return "[dout]" + super.toString();
    }
}
