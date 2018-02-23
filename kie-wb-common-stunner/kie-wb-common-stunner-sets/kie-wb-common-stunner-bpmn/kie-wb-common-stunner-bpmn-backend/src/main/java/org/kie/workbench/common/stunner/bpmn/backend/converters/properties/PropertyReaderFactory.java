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

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.BoundaryEvent;
import org.eclipse.bpmn2.BusinessRuleTask;
import org.eclipse.bpmn2.CatchEvent;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.Lane;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.ScriptTask;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.SubProcess;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.ThrowEvent;
import org.eclipse.bpmn2.UserTask;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.kie.workbench.common.stunner.bpmn.backend.converters.DefinitionResolver;

public class PropertyReaderFactory {

    private final BPMNPlane plane;
    private final DefinitionResolver definitionResolver;

    public PropertyReaderFactory(DefinitionResolver definitionResolver) {
        this.plane = definitionResolver.getPlane();
        this.definitionResolver = definitionResolver;
    }

    public FlowElementPropertyReader of(FlowElement el) {
        return new FlowElementPropertyReader(el, plane, definitionResolver.getShape(el.getId()));
    }

    public LanePropertyReader of(Lane el) {
        return new LanePropertyReader(el, plane, definitionResolver.getShape(el.getId()));
    }

    public SequenceFlowPropertyReader of(SequenceFlow el) {
        return new SequenceFlowPropertyReader(el, plane, definitionResolver);
    }

    public GatewayPropertyReader of(Gateway el) {
        return new GatewayPropertyReader(el, plane, definitionResolver.getShape(el.getId()));
    }

    public TaskPropertyReader of(Task el) {
        return new TaskPropertyReader(el, plane, definitionResolver);
    }

    public UserTaskPropertyReader of(UserTask el) {
        return new UserTaskPropertyReader(el, plane, definitionResolver);
    }

    public ScriptTaskPropertyReader of(ScriptTask el) {
        return new ScriptTaskPropertyReader(el, plane, definitionResolver);
    }

    public BusinessRuleTaskPropertyReader of(BusinessRuleTask el) {
        return new BusinessRuleTaskPropertyReader(el, plane, definitionResolver);
    }

    public ActivityPropertyReader of(Activity el) {
        return new ActivityPropertyReader(el, plane, definitionResolver);
    }

    public EventPropertyReader of(Event el) {
        if (el instanceof BoundaryEvent) {
            return new BoundaryEventPropertyReader((BoundaryEvent) el, plane, definitionResolver);
        } else if (el instanceof CatchEvent) {
            CatchEvent catchEvent = (CatchEvent) el;
            return new CatchEventPropertyReader(catchEvent, plane, definitionResolver);
        } else if (el instanceof ThrowEvent) {
            ThrowEvent throwEvent = (ThrowEvent) el;
            return new ThrowEventPropertyReader(throwEvent, plane, definitionResolver);
        } else {
            throw new IllegalArgumentException(el.toString());
        }
    }

    public SubProcessPropertyReader of(SubProcess el) {
        return new SubProcessPropertyReader(el, plane, definitionResolver);
    }

    public ProcessPropertyReader of(Process el) {
        return new ProcessPropertyReader(el, plane, definitionResolver.getShape(el.getId()));
    }
}
