package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import java.util.UUID;

import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.PotentialOwner;
import org.eclipse.bpmn2.ResourceAssignmentExpression;
import org.eclipse.bpmn2.UserTask;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.jboss.drools.OnEntryScriptType;
import org.jboss.drools.OnExitScriptType;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.CustomElement;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.CustomInput;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.Scripts;
import org.kie.workbench.common.stunner.bpmn.definition.property.assignee.Actors;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.OnEntryAction;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.OnExitAction;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ScriptTypeListValue;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ScriptTypeValue;

import static org.jboss.drools.DroolsPackage.Literals.DOCUMENT_ROOT__ON_ENTRY_SCRIPT;
import static org.jboss.drools.DroolsPackage.Literals.DOCUMENT_ROOT__ON_EXIT_SCRIPT;
import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;
import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.droolsFactory;

public class UserTaskPropertyWriter extends ActivityPropertyWriter {

    private final UserTask task;
    private final CustomInput<String> description;
    private final CustomInput<String> createdBy;
    private final CustomInput<String> taskName;
    private final CustomInput<String> groupId;
    private final CustomInput<Boolean> skippable;
    private final CustomInput<String> priority;
    private final CustomInput<String> subject;

    public UserTaskPropertyWriter(UserTask task) {
        super(task);
        this.task = task;

        this.skippable = CustomInput.skippable.of(task);
        this.addBaseElement(this.skippable.typeDef());

        this.priority = CustomInput.priority.of(task);
        this.addBaseElement(this.priority.typeDef());

        this.subject = CustomInput.subject.of(task);
        this.addBaseElement(this.subject.typeDef());

        this.description = CustomInput.description.of(task);
        this.addBaseElement(this.description.typeDef());

        this.createdBy = CustomInput.createdBy.of(task);
        this.addBaseElement(this.createdBy.typeDef());

        this.taskName = CustomInput.taskName.of(task);
        this.addBaseElement(this.taskName.typeDef());

        this.groupId = CustomInput.groupId.of(task);
        this.addBaseElement(this.skippable.typeDef());
    }

    public void setAsync(boolean async) {
        CustomElement.async.of(task).set(async);
    }

    public void setSkippable(boolean skippable) {
        this.skippable.set(skippable);
    }

    public void setPriority(String priority) {
        this.priority.set(priority);
    }

    public void setSubject(String subject) {
        this.subject.set(subject);
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy.set(createdBy);
    }

    public void setAdHocAutostart(boolean autoStart) {
        CustomElement.autoStart.of(task).set(autoStart);
    }

    public void setTaskName(String taskName) {
        this.taskName.set(taskName);
    }

    public void setActors(Actors actors) {
        for (String actor : actors.getValue().split(",")) {
            PotentialOwner potentialOwner = bpmn2.createPotentialOwner();
            potentialOwner.setId(UUID.randomUUID().toString());

            FormalExpression formalExpression = bpmn2.createFormalExpression();
            formalExpression.setBody(actor);

            ResourceAssignmentExpression resourceAssignmentExpression =
                    bpmn2.createResourceAssignmentExpression();
            resourceAssignmentExpression.setExpression(formalExpression);

            potentialOwner.setResourceAssignmentExpression(resourceAssignmentExpression);

            task.getResources().add(potentialOwner);
        }
    }

    public void setGroupId(String value) {
        groupId.set(asCData(value));
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

    private EStructuralFeatureImpl.SimpleFeatureMapEntry entryOf(EReference eref, Object script) {
        return new EStructuralFeatureImpl.SimpleFeatureMapEntry(
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
