package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import java.util.UUID;

import org.eclipse.bpmn2.DataInput;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.InputOutputSpecification;
import org.eclipse.bpmn2.InputSet;
import org.eclipse.bpmn2.PotentialOwner;
import org.eclipse.bpmn2.ResourceAssignmentExpression;
import org.eclipse.bpmn2.UserTask;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.jboss.drools.OnEntryScriptType;
import org.jboss.drools.OnExitScriptType;
import org.kie.workbench.common.stunner.bpmn.backend.converters.properties.Scripts;
import org.kie.workbench.common.stunner.bpmn.definition.property.assignee.Actors;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.AssignmentsInfo;
import org.kie.workbench.common.stunner.bpmn.definition.property.simulation.SimulationSet;
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

    public UserTaskPropertyWriter(UserTask task) {
        super(task);
        this.task = task;
    }

    public void setAsync(boolean async) {
        setMeta("customAsync", String.valueOf(async));
    }

    public void setSkippable(boolean skippable) {
        setInput("Skippable", String.valueOf(skippable));
    }

    public void setPriority(String priority) {
        setInput("Priority", priority);
    }

    public void setSubject(String subject) {
        setInput("Comment", subject);
    }

    public void setDescription(String description) {
        setInput("Description", description);
    }

    public void setCreatedBy(String createdBy) {
        setInput("CreatedBy", createdBy);
    }

    public void setAdHocAutostart(boolean autoStart) {
        setMeta("customAutoStart", String.valueOf(autoStart));
    }

//    public void setAssignmentsInfo(AssignmentsInfo assignmentsInfo) {
//        InputOutputSpecification ioSpec = bpmn2.createInputOutputSpecification();
//        task.setIoSpecification(ioSpec);
//        assignmentsInfo.getAssociations().getInputs()
//                .stream()
//                .map(as -> this.addDataInputAssociation(
//                        as, assignmentsInfo.getInputs()))
//                .forEach(dia -> {
//                    InputSet inputSet = bpmn2.createInputSet();
//                    inputSet.getDataInputRefs().add((DataInput) dia.getTargetRef());
//                    ioSpec.getDataInputs().add((DataInput) dia.getTargetRef());
//                    ioSpec.getInputSets().add(inputSet);
//                    dia.getSourceRef().forEach(this::addBaseElement);
//                    task.getDataInputAssociations().add(dia);
//                });
//    }

    public void setTaskName(String taskName) {
        //task.setName(taskName.trim());
        setInput("TaskName", taskName);
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
        setInput("GroupId", asCData(value));
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
