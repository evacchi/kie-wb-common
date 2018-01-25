/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.bpmn.definition;

import javax.validation.Valid;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormDefinition;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormField;
import org.kie.workbench.common.forms.adf.definitions.settings.FieldPolicy;
import org.kie.workbench.common.stunner.bpmn.definition.property.background.BackgroundSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.DataIOSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.HasDataIOSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.CircleDimensionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.Radius;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.error.CancellingErrorEventExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.font.FontSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.core.definition.annotation.Definition;
import org.kie.workbench.common.stunner.core.definition.annotation.PropertySet;
import org.kie.workbench.common.stunner.core.definition.annotation.morph.Morph;
import org.kie.workbench.common.stunner.core.definition.builder.Builder;
import org.kie.workbench.common.stunner.core.factory.graph.NodeFactory;
import org.kie.workbench.common.stunner.core.util.HashUtil;

@Portable
@Bindable
@Definition(graphFactory = NodeFactory.class, builder = IntermediateErrorEventCatching.IntermediateErrorEventCatchingBuilder.class)
@Morph(base = BaseCatchingIntermediateEvent.class)
@FormDefinition(
        startElement = "general",
        policy = FieldPolicy.ONLY_MARKED
)
public class IntermediateErrorEventCatching extends BaseCatchingIntermediateEvent implements Executable<CancellingErrorEventExecutionSet>,
                                                                                             HasDataIOSet {

    @PropertySet
    @FormField(afterElement = "general")
    @Valid
    protected CancellingErrorEventExecutionSet executionSet;

    @PropertySet
    @FormField(afterElement = "executionSet")
    @Valid
    protected DataIOSet dataIOSet;

    public IntermediateErrorEventCatching() {
    }

    public IntermediateErrorEventCatching(final @MapsTo("general") BPMNGeneralSet general,
                                          final @MapsTo("backgroundSet") BackgroundSet backgroundSet,
                                          final @MapsTo("fontSet") FontSet fontSet,
                                          final @MapsTo("dimensionsSet") CircleDimensionSet dimensionsSet,
                                          final @MapsTo("dataIOSet") DataIOSet dataIOSet,
                                          final @MapsTo("executionSet") CancellingErrorEventExecutionSet executionSet) {
        super(general,
              backgroundSet,
              fontSet,
              dimensionsSet);
        this.dataIOSet = dataIOSet;
        this.executionSet = executionSet;
    }

    @Override
    protected void initLabels() {
        super.initLabels();
        labels.remove("sequence_end");
        labels.remove("FromEventbasedGateway");
    }

    public DataIOSet getDataIOSet() {
        return dataIOSet;
    }

    public void setDataIOSet(DataIOSet dataIOSet) {
        this.dataIOSet = dataIOSet;
    }

    public CancellingErrorEventExecutionSet getExecutionSet() {
        return executionSet;
    }

    public void setExecutionSet(CancellingErrorEventExecutionSet executionSet) {
        this.executionSet = executionSet;
    }

    @Override
    public boolean hasOutputVars() {
        return true;
    }

    @Override
    public boolean isSingleOutputVar() {
        return true;
    }

    @Override
    public int hashCode() {
        return HashUtil.combineHashCodes(super.hashCode(),
                                         executionSet.hashCode(),
                                         dataIOSet.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof IntermediateErrorEventCatching) {
            IntermediateErrorEventCatching other = (IntermediateErrorEventCatching) o;
            return super.equals(other) &&
                    executionSet.equals(other.executionSet) &&
                    dataIOSet.equals(other.dataIOSet);
        }
        return false;
    }

    @NonPortable
    public static class IntermediateErrorEventCatchingBuilder implements Builder<IntermediateErrorEventCatching> {

        @Override
        public IntermediateErrorEventCatching build() {
            return new IntermediateErrorEventCatching(new BPMNGeneralSet(""),
                                                      new BackgroundSet(),
                                                      new FontSet(),
                                                      new CircleDimensionSet(new Radius()),
                                                      new DataIOSet(),
                                                      new CancellingErrorEventExecutionSet());
        }
    }
}