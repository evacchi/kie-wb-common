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

import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.di.BpmnDiFactory;
import org.eclipse.dd.dc.DcFactory;
import org.kie.workbench.common.stunner.bpmn.backend.legacy.util.Utils;

public class FlowElementPropertyWriter {

    private final FlowElement flowElement;
    private final BpmnDiFactory di = BpmnDiFactory.eINSTANCE;
    private final DcFactory dc = DcFactory.eINSTANCE;

    public FlowElementPropertyWriter(FlowElement flowElement) {
        this.flowElement = flowElement;
    }

    public void setName(String value) {
        flowElement.setName(value);
        setMeta("elementname", value);
    }

    private void setMeta(String attributeId, String value) {
        Utils.setMetaDataExtensionValue(
                flowElement,
                attributeId,
                value);
    }
}
