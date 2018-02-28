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

package org.kie.workbench.common.stunner.bpmn.backend.converters.properties;

import java.util.Collections;

import org.eclipse.bpmn2.ThrowEvent;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.kie.workbench.common.stunner.bpmn.backend.converters.DefinitionResolver;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;

class ThrowEventPropertyReader extends EventPropertyReader {

    private final ThrowEvent throwEvent;

    public ThrowEventPropertyReader(ThrowEvent throwEvent, BPMNPlane plane, DefinitionResolver definitionResolver) {
        super(throwEvent, plane, definitionResolver, EventPropertyReader.getSignalRefId(throwEvent.getEventDefinitions()));
        this.throwEvent = throwEvent;
    }

    @Override
    public AssignmentsInfo getAssignmentsInfo() {
        return AssignmentsInfos.of(
                throwEvent.getDataInputs(),
                throwEvent.getDataInputAssociation(),
                Collections.emptyList(),
                Collections.emptyList(), false);
    }

//    @Override
//    protected String colorsDefaultBg() {
//        return throwEvent instanceof EndEvent ? Colors.defaultBgColor_EndEvents : Colors.defaultBgColor_ThrowingEvents;
//    }
//
//    @Override
//    protected String colorsDefaultBr() {
//        return Colors.defaultBgColor_ThrowingEvents;
//    }
}
