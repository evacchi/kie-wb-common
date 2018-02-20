package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.tasks;

import org.eclipse.bpmn2.BusinessRuleTask;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl.SimpleFeatureMapEntry;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.jboss.drools.OnEntryScriptType;
import org.jboss.drools.OnExitScriptType;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.Scripts;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties.ActivityPropertyWriter;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.OnEntryAction;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.OnExitAction;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.RuleFlowGroup;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ScriptTypeListValue;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ScriptTypeValue;

import static org.jboss.drools.DroolsPackage.Literals.DOCUMENT_ROOT__ON_ENTRY_SCRIPT;
import static org.jboss.drools.DroolsPackage.Literals.DOCUMENT_ROOT__ON_EXIT_SCRIPT;
import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;
import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.droolsFactory;

public class BusinessRuleTaskPropertyWriter extends ActivityPropertyWriter {

    public BusinessRuleTaskPropertyWriter(BusinessRuleTask task) {
        super(task);
    }

    public void setAsync(Boolean value) {
        setMeta("customAsync", String.valueOf(value));
    }

    public void setRuleFlowGroup(RuleFlowGroup ruleFlowGroup) {
        String value = ruleFlowGroup.getValue();
        FeatureMap.Entry attribute = attribute("ruleFlowGroup", value);
        getFlowElement().getAnyAttribute().add(attribute);
    }

    public void setAdHocAutostart(Boolean value) {
        setMeta("customAutoStart", String.valueOf(value));
    }

    public void setOnEntryAction(OnEntryAction onEntryAction) {
        ScriptTypeListValue value = onEntryAction.getValue();
        for (ScriptTypeValue scriptTypeValue : value.getValues()) {
            if (scriptTypeValue.getScript() == null && scriptTypeValue.getScript().isEmpty()) {
                continue;
            }
            OnEntryScriptType script = droolsFactory.createOnEntryScriptType();
            script.setScript(asCData(scriptTypeValue.getScript()));
            String scriptLanguage = Scripts.scriptLanguageToUri(scriptTypeValue.getLanguage());
            script.setScriptFormat(scriptLanguage);
            addExtensionValue(DOCUMENT_ROOT__ON_ENTRY_SCRIPT, script);
        }
    }

    public void setOnExitAction(OnExitAction onExitAction) {
        ScriptTypeListValue value = onExitAction.getValue();
        for (ScriptTypeValue scriptTypeValue : value.getValues()) {
            if (scriptTypeValue.getScript() == null && scriptTypeValue.getScript().isEmpty()) {
                continue;
            }
            OnExitScriptType script = droolsFactory.createOnExitScriptType();
            script.setScript(asCData(scriptTypeValue.getScript()));
            String scriptLanguage = Scripts.scriptLanguageToUri(scriptTypeValue.getLanguage());
            script.setScriptFormat(scriptLanguage);
            addExtensionValue(DOCUMENT_ROOT__ON_EXIT_SCRIPT, script);
        }
    }

    private SimpleFeatureMapEntry entryOf(EReference eref, Object script) {
        return new SimpleFeatureMapEntry(
                (EStructuralFeature.Internal) eref,
                script);
    }

    protected void addExtensionValue(EReference eref, Object value) {
        FeatureMap.Entry entry = entryOf(eref, value);
        addExtensionValue(entry);
    }

    protected void addExtensionValue(FeatureMap.Entry value) {
        ExtensionAttributeValue eav = bpmn2.createExtensionAttributeValue();
        flowElement.getExtensionValues().add(eav);
        eav.getValue().add(value);
    }
}
