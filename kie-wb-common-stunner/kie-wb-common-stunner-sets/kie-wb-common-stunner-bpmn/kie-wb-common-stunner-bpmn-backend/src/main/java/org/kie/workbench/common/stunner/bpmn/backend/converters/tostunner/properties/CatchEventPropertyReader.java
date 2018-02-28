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

import java.util.Collections;

import org.eclipse.bpmn2.CatchEvent;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tostunner.DefinitionResolver;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;

class CatchEventPropertyReader extends EventPropertyReader {

    private final CatchEvent catchEvent;

    public CatchEventPropertyReader(CatchEvent catchEvent, BPMNPlane plane, DefinitionResolver definitionResolver) {
        super(catchEvent, plane, definitionResolver, EventPropertyReader.getSignalRefId(catchEvent.getEventDefinitions()));
        this.catchEvent = catchEvent;
    }

    @Override
    public AssignmentsInfo getAssignmentsInfo() {
        return AssignmentsInfos.of(
                Collections.emptyList(),
                Collections.emptyList(),
                catchEvent.getDataOutputs(),
                catchEvent.getDataOutputAssociation(),
                false);
    }

//    @Override
//    protected String colorsDefaultBg() {
//        return catchEvent instanceof StartEvent ? "#9acd32" : Colors.defaultBgColor_CatchingEvents;
//    }

//    protected String colorsDefaultBr() {
//        return Colors.defaultBrColor_CatchingEvents;
//    }
}
