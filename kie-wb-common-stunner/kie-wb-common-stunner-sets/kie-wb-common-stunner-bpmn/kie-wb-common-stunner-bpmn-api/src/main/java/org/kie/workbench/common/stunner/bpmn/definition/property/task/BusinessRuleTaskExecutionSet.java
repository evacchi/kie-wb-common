/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.kie.workbench.common.stunner.bpmn.definition.property.task;

import javax.validation.Valid;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.kie.workbench.common.forms.adf.definitions.annotations.FieldParam;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormDefinition;
import org.kie.workbench.common.forms.adf.definitions.annotations.FormField;
import org.kie.workbench.common.forms.adf.definitions.annotations.field.selector.SelectorDataProvider;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.selectors.listBox.type.ListBoxFieldType;
import org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textArea.type.TextAreaFieldType;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNPropertySet;
import org.kie.workbench.common.stunner.bpmn.forms.model.ComboBoxFieldType;
import org.kie.workbench.common.stunner.core.definition.annotation.Property;
import org.kie.workbench.common.stunner.core.definition.annotation.PropertySet;
import org.kie.workbench.common.stunner.core.util.HashUtil;

@Portable
@Bindable
@PropertySet
@FormDefinition
public class BusinessRuleTaskExecutionSet implements BPMNPropertySet,
                                                     ScriptableExecutionSet {

    @Property
    @FormField(
            type = ComboBoxFieldType.class
    )
    @SelectorDataProvider(
            type = SelectorDataProvider.ProviderType.REMOTE,
            className = "org.kie.workbench.common.stunner.bpmn.backend.dataproviders.RuleFlowGroupFormProvider")
    @Valid
    protected RuleFlowGroup ruleFlowGroup;
    @Property
    @FormField(
            type = ListBoxFieldType.class,
            afterElement = "onExitAction"
    )
    @SelectorDataProvider(
            type = SelectorDataProvider.ProviderType.REMOTE,
            className = "org.kie.workbench.common.stunner.bpmn.backend.dataproviders.ScriptLanguageFormProvider")
    @Valid
    protected ScriptLanguage scriptLanguage;
    @Property
    @FormField(
            type = TextAreaFieldType.class,
            afterElement = "ruleFlowGroup",
            settings = {@FieldParam(name = "rows", value = "5")}
    )
    @Valid
    private OnEntryAction onEntryAction;
    @Property
    @FormField(
            type = TextAreaFieldType.class,
            afterElement = "onEntryAction",
            settings = {@FieldParam(name = "rows", value = "5")}
    )
    @Valid
    private OnExitAction onExitAction;
    @Property
    @FormField(
            afterElement = "scriptLanguage"
    )
    @Valid
    private IsAsync isAsync;

    @Property
    @FormField(
            afterElement = "isAsync"
    )
    @Valid
    private AdHocAutostart adHocAutostart;

    public BusinessRuleTaskExecutionSet() {
        this(new RuleFlowGroup(),
             new OnEntryAction(""),
             new OnExitAction(""),
             new ScriptLanguage(),
             new IsAsync(),
             new AdHocAutostart());
    }

    public BusinessRuleTaskExecutionSet(final @MapsTo("ruleFlowGroup") RuleFlowGroup ruleFlowGroup,
                                        final @MapsTo("onEntryAction") OnEntryAction onEntryAction,
                                        final @MapsTo("onExitAction") OnExitAction onExitAction,
                                        final @MapsTo("scriptLanguage") ScriptLanguage scriptLanguage,
                                        final @MapsTo("isAsync") IsAsync isAsync,
                                        final @MapsTo("adHocAutostart") AdHocAutostart adHocAutostart) {
        this.ruleFlowGroup = ruleFlowGroup;
        this.onEntryAction = onEntryAction;
        this.onExitAction = onExitAction;
        this.scriptLanguage = scriptLanguage;
        this.isAsync = isAsync;
        this.adHocAutostart = adHocAutostart;
    }

    public RuleFlowGroup getRuleFlowGroup() {
        return ruleFlowGroup;
    }

    public void setRuleFlowGroup(final RuleFlowGroup ruleFlowGroup) {
        this.ruleFlowGroup = ruleFlowGroup;
    }

    @Override
    public OnEntryAction getOnEntryAction() {
        return onEntryAction;
    }

    @Override
    public void setOnEntryAction(OnEntryAction onEntryAction) {
        this.onEntryAction = onEntryAction;
    }

    @Override
    public OnExitAction getOnExitAction() {
        return onExitAction;
    }

    @Override
    public void setOnExitAction(OnExitAction onExitAction) {
        this.onExitAction = onExitAction;
    }

    @Override
    public ScriptLanguage getScriptLanguage() {
        return scriptLanguage;
    }

    @Override
    public void setScriptLanguage(ScriptLanguage scriptLanguage) {
        this.scriptLanguage = scriptLanguage;
    }

    public IsAsync getIsAsync() {
        return isAsync;
    }

    public void setIsAsync(IsAsync isAsync) {
        this.isAsync = isAsync;
    }

    public AdHocAutostart getAdHocAutostart() {
        return adHocAutostart;
    }

    public void setAdHocAutostart(AdHocAutostart adHocAutostart) {
        this.adHocAutostart = adHocAutostart;
    }

    @Override
    public int hashCode() {
        return HashUtil.combineHashCodes(ruleFlowGroup.hashCode(),
                                         onEntryAction.hashCode(),
                                         onExitAction.hashCode(),
                                         scriptLanguage.hashCode(),
                                         isAsync.hashCode(),
                                         adHocAutostart.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BusinessRuleTaskExecutionSet) {
            BusinessRuleTaskExecutionSet other = (BusinessRuleTaskExecutionSet) o;
            return ruleFlowGroup.equals(other.ruleFlowGroup) &&
                    onEntryAction.equals(other.onEntryAction) &&
                    onExitAction.equals(other.onExitAction) &&
                    scriptLanguage.equals(other.scriptLanguage) &&
                    isAsync.equals(other.isAsync) &&
                    adHocAutostart.equals(other.adHocAutostart);
        }
        return false;
    }
}
