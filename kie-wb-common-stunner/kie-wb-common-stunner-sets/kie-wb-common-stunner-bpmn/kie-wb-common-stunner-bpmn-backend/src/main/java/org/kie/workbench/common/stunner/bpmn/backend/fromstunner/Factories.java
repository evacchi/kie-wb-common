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

package org.kie.workbench.common.stunner.bpmn.backend.fromstunner;

import bpsim.BpsimFactory;
import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.di.BpmnDiFactory;
import org.eclipse.dd.dc.DcFactory;

public class Factories {

    public static final Bpmn2Factory bpmn2 = Bpmn2Factory.eINSTANCE;
    public static final BpsimFactory bpsim = BpsimFactory.eINSTANCE;
    public static final BpmnDiFactory di = BpmnDiFactory.eINSTANCE;
    public static final DcFactory dc = DcFactory.eINSTANCE;
}
