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

import org.eclipse.bpmn2.ScriptTask;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.jboss.drools.DroolsPackage;
import org.jboss.drools.OnEntryScriptType;
import org.jboss.drools.OnExitScriptType;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.Scripts;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ScriptTypeValue;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.droolsFactory;

public class ScriptTaskPropertyWriter extends PropertyWriter {

    private final ScriptTask scriptTask;

    public ScriptTaskPropertyWriter(ScriptTask scriptTask) {
        super(scriptTask);
        this.scriptTask = scriptTask;
    }

    public void setScript(ScriptTypeValue script) {
        scriptTask.setScriptFormat(
                Scripts.scriptLanguageToUri(script.getLanguage()));
        scriptTask.setScript(asCData(script.getScript()));
    }

    public void setAsync(Boolean async) {
        setMeta("customAsync", String.valueOf(async));
    }

    public FeatureMap.Entry setOnEntryScript(ScriptTypeValue onEntryAction) {
        if (onEntryAction.getScript() == null && onEntryAction.getScript().isEmpty()) {
            return null;
        }
        OnEntryScriptType script = droolsFactory.createOnEntryScriptType();
        script.setScript(asCData(onEntryAction.getScript()));
        String scriptLanguage = Scripts.scriptLanguageToUri(onEntryAction.getLanguage());
        script.setScriptFormat(scriptLanguage);
        return new EStructuralFeatureImpl.SimpleFeatureMapEntry(
                (EStructuralFeature.Internal) DroolsPackage.Literals.DOCUMENT_ROOT__ON_ENTRY_SCRIPT,
                script);
    }

    public FeatureMap.Entry setOnExitScript(ScriptTypeValue onExitAction) {
        if (onExitAction.getScript() == null && onExitAction.getScript().isEmpty()) {
            return null;
        }
        OnExitScriptType script = droolsFactory.createOnExitScriptType();
        script.setScript(asCData(onExitAction.getScript()));
        String scriptLanguage = Scripts.scriptLanguageToUri(onExitAction.getLanguage());
        script.setScriptFormat(scriptLanguage);
        return new EStructuralFeatureImpl.SimpleFeatureMapEntry(
                (EStructuralFeature.Internal) DroolsPackage.Literals.DOCUMENT_ROOT__ON_EXIT_SCRIPT,
                script);
    }
}