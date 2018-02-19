/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormDefinition;
import org.kie.workbench.common.forms.adf.definitions.settings.FieldPolicy;
import org.kie.workbench.common.stunner.bpmn.definition.property.background.BackgroundSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.CircleDimensionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.dimensions.Radius;
import org.kie.workbench.common.stunner.bpmn.definition.property.font.FontSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.general.BPMNGeneralSet;
import org.kie.workbench.common.stunner.core.definition.annotation.Definition;
import org.kie.workbench.common.stunner.core.definition.annotation.morph.Morph;
import org.kie.workbench.common.stunner.core.definition.builder.Builder;
import org.kie.workbench.common.stunner.core.factory.graph.NodeFactory;

@Portable
@Bindable
@Definition(graphFactory = NodeFactory.class, builder = EndNoneEvent.EndNoneEventBuilder.class)
@Morph(base = BaseEndEvent.class)
@FormDefinition(
        startElement = "general",
        policy = FieldPolicy.ONLY_MARKED
)
public class EndNoneEvent extends BaseEndEvent {

    public EndNoneEvent() {
    }

    public EndNoneEvent(final @MapsTo("general") BPMNGeneralSet general,
                        final @MapsTo("backgroundSet") BackgroundSet backgroundSet,
                        final @MapsTo("fontSet") FontSet fontSet,
                        final @MapsTo("dimensionsSet") CircleDimensionSet dimensionsSet) {
        super(general,
              backgroundSet,
              fontSet,
              dimensionsSet);
    }

    @NonPortable
    public static class EndNoneEventBuilder implements Builder<EndNoneEvent> {

        @Override
        public EndNoneEvent build() {
            return new EndNoneEvent(new BPMNGeneralSet(""),
                                    new BackgroundSet(),
                                    new FontSet(),
                                    new CircleDimensionSet(new Radius()));
        }
    }
}
