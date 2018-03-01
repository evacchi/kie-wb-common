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

package org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.properties;

import java.util.List;
import java.util.Objects;

import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.SignalEventDefinition;
import org.eclipse.bpmn2.TimerEventDefinition;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.kie.workbench.common.stunner.bpmn.backend.converters.customproperties.CustomAttribute;
import org.kie.workbench.common.stunner.bpmn.backend.converters.customproperties.CustomElement;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.DefinitionResolver;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.timer.TimerSettings;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.timer.TimerSettingsValue;
import org.kie.workbench.common.stunner.bpmn.definition.property.simulation.SimulationAttributeSet;

public abstract class EventPropertyReader extends FlowElementPropertyReader {

    private final DefinitionResolver definitionResolver;
    private String signalRefId = null;

    EventPropertyReader(Event element, BPMNPlane plane, DefinitionResolver definitionResolver, String eventDefinition) {
        super(element, plane, definitionResolver.getShape(element.getId()));
        this.definitionResolver = definitionResolver;
        this.signalRefId = eventDefinition;
    }

    static String getSignalRefId(List<EventDefinition> eventDefinitions) {
        if (eventDefinitions.size() == 1 && eventDefinitions.get(0) instanceof SignalEventDefinition) {
            return ((SignalEventDefinition) eventDefinitions.get(0)).getSignalRef();
        }
        return null;
    }

    public String getSignalScope() {
        return CustomElement.scope.of(element).get();
    }

    public abstract AssignmentsInfo getAssignmentsInfo();

    public boolean isCancelActivity() {
        return CustomAttribute.boundarycaForEvent.of(element).get();
    }

    public TimerSettingsValue getTimerSettings(TimerEventDefinition eventDefinition) {
        TimerSettingsValue timerSettingsValue = new TimerSettings().getValue();
        FormalExpression timeCycle = (FormalExpression) eventDefinition.getTimeCycle();
        if (timeCycle != null) {
            timerSettingsValue.setTimeCycle(timeCycle.getBody());
            timerSettingsValue.setTimeCycleLanguage(timeCycle.getLanguage());
        }

        FormalExpression timeDate = (FormalExpression) eventDefinition.getTimeDate();
        if (timeDate != null) {
            timerSettingsValue.setTimeDate(timeDate.getBody());
        }

        FormalExpression timeDateDuration = (FormalExpression) eventDefinition.getTimeDuration();
        if (timeDateDuration != null) {
            timerSettingsValue.setTimeDuration(timeDateDuration.getBody());
        }
        return (timerSettingsValue);
    }

    public String getSignalRef() {
        Objects.requireNonNull(signalRefId);
        return definitionResolver.resolveSignalName(signalRefId);
    }

    public SimulationAttributeSet getSimulationSet() {
        return definitionResolver.resolveSimulationParameters(element.getId())
                .map(SimulationAttributeSets::of)
                .orElse(new SimulationAttributeSet());
    }

//    @Override
//    protected String colorsDefaultBg() {
//        return Colors.defaultBgColor_Events;
//    }
}
