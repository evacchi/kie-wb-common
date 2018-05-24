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

import java.util.Objects;

import javax.annotation.Nullable;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.kie.workbench.common.forms.adf.definitions.annotations.metaModel.FieldDefinition;
import org.kie.workbench.common.forms.adf.definitions.annotations.metaModel.I18nMode;
import org.kie.workbench.common.stunner.core.definition.annotation.Property;

@Portable
@Bindable
@Property
@FieldDefinition(i18nMode = I18nMode.OVERRIDE_I18N_KEY)
public class TypedAssignmentsInfo extends AssignmentsInfo {

    private String inputTypeName;

    private String outputTypeName;

    public TypedAssignmentsInfo() {
        super();
    }

    public TypedAssignmentsInfo(
            String value,
            @Nullable String inputTypeName,
            @Nullable String outputTypeName) {
        super(value);
        this.inputTypeName = inputTypeName;
        this.outputTypeName = outputTypeName;
    }

    public void setInputTypeName(String inputTypeName) {
        this.inputTypeName = inputTypeName;
    }

    public void setOutputTypeName(String outputTypeName) {
        this.outputTypeName = outputTypeName;
    }

    public String getInputTypeName() {
        return inputTypeName;
    }

    public String getOutputTypeName() {
        return outputTypeName;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TypedAssignmentsInfo) {
            TypedAssignmentsInfo other = (TypedAssignmentsInfo) o;
            return super.equals(o) &&
                    Objects.equals(inputTypeName, other.inputTypeName) &&
                    Objects.equals(outputTypeName, other.outputTypeName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getValue(),
                inputTypeName,
                outputTypeName);
    }

    @Override
    public String toString() {
        return "TypedAssignmentsInfo{" +
                "value='" + getValue() + '\'' +
                "inputTypeName='" + inputTypeName + '\'' +
                ", outputTypeName='" + outputTypeName + '\'' +
                "} ";
    }
}
