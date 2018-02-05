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

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.kie.workbench.common.stunner.bpmn.backend.converters.Colors;

public class GatewayPropertyReader extends BasePropertyReader {

    public GatewayPropertyReader(BaseElement element, BPMNPlane plane) {
        super(element, plane);
    }

    public String getDefaultRoute() {
        return attribute("dg");
    }

    @Override
    protected String colorsDefaultBg() {
        return Colors.defaultBgColor_Gateways;
    }

    @Override
    protected String colorsDefaultBr() {
        return Colors.defaultBrColor_Gateways;
    }

}
