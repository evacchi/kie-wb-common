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

package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import org.eclipse.bpmn2.Error;
import org.eclipse.bpmn2.ErrorEventDefinition;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.Message;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.Signal;
import org.eclipse.bpmn2.SignalEventDefinition;
import org.eclipse.bpmn2.TerminateEventDefinition;
import org.eclipse.bpmn2.TimerEventDefinition;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Ids;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.error.ErrorRef;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.message.MessageRef;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.signal.SignalRef;
import org.kie.workbench.common.stunner.bpmn.definition.property.event.timer.TimerSettings;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;

public abstract class EventPropertyWriter extends PropertyWriter {

    public EventPropertyWriter(Event event) {
        super(event);
    }

    public abstract void setAssignmentsInfo(AssignmentsInfo assignmentsInfo);

    public void addMessage(MessageRef messageRef) {
        Message message = bpmn2.createMessage();
        MessageEventDefinition messageEventDefinition =
                bpmn2.createMessageEventDefinition();

        message.setName(messageRef.getValue());
        messageEventDefinition.setMessageRef(message);

        addEventDefinition(messageEventDefinition);
        addBaseElement(message);
    }

    public void addSignal(SignalRef signalRef) {
        SignalEventDefinition signalEventDefinition =
                bpmn2.createSignalEventDefinition();

        Signal signal = bpmn2.createSignal();
        String name = signalRef.getValue();
        signal.setName(name);
        signal.setId(Ids.fromString(name));
        signalEventDefinition.setSignalRef(signal.getId());

        addEventDefinition(signalEventDefinition);
        addBaseElement(signal);
    }

    public void addError(ErrorRef errorRef) {
        Error error = bpmn2.createError();
        ErrorEventDefinition errorEventDefinition =
                bpmn2.createErrorEventDefinition();

        String errorCode = errorRef.getValue();
        error.setId(errorCode);
        error.setErrorCode(errorCode);
        errorEventDefinition.setErrorRef(error);

        FeatureMap.Entry erefname =
                Attributes.drools("erefname", errorCode);
        errorEventDefinition.getAnyAttribute().add(erefname);

        addEventDefinition(errorEventDefinition);
        addBaseElement(error);
    }

    public void addTerminate() {
        TerminateEventDefinition terminateEventDefinition =
                bpmn2.createTerminateEventDefinition();
        addEventDefinition(terminateEventDefinition);
    }

    public void addTimer(TimerSettings timerSettings) {
        TimerEventDefinition timerEventDefinition =
                bpmn2.createTimerEventDefinition();
        addEventDefinition(timerEventDefinition);
    }

    protected abstract void addEventDefinition(EventDefinition eventDefinition);
}
