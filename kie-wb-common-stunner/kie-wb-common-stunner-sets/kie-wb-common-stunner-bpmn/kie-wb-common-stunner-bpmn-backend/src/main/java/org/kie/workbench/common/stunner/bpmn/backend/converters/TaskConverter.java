package org.kie.workbench.common.stunner.bpmn.backend.converters;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.bpmn2.DataInputAssociation;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.PotentialOwner;
import org.eclipse.bpmn2.ResourceRole;
import org.eclipse.bpmn2.Task;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.jboss.drools.DroolsPackage;
import org.jboss.drools.OnEntryScriptType;
import org.jboss.drools.OnExitScriptType;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNViewDefinition;
import org.kie.workbench.common.stunner.bpmn.definition.BusinessRuleTask;
import org.kie.workbench.common.stunner.bpmn.definition.NoneTask;
import org.kie.workbench.common.stunner.bpmn.definition.ScriptTask;
import org.kie.workbench.common.stunner.bpmn.definition.UserTask;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.BusinessRuleTaskExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.OnEntryAction;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.OnExitAction;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ScriptLanguage;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.ScriptableExecutionSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.task.UserTaskExecutionSet;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.view.View;

import static org.kie.workbench.common.stunner.bpmn.backend.converters.Properties.*;

public class TaskConverter {

    private TypedFactoryManager factoryManager;

    public TaskConverter(TypedFactoryManager factoryManager) {
        this.factoryManager = factoryManager;
    }

    public Node<? extends View<? extends BPMNViewDefinition>, ?> convert(org.eclipse.bpmn2.Task task) {
        List<ExtensionAttributeValue> extensionValues = task.getExtensionValues();
        List<DataInputAssociation> inputAssociations = task.getDataInputAssociations();

        return Match.ofNode(Task.class, BPMNViewDefinition.class)
                .when(org.eclipse.bpmn2.BusinessRuleTask.class, t -> {
                    Node<View<BusinessRuleTask>, Edge> node = factoryManager.newNode(t.getId(), BusinessRuleTask.class);
                    BusinessRuleTask taskDef = node.getContent().getDefinition();
                    AssignmentsInfoStringBuilder.setAssignmentsInfo(
                            task, taskDef.getDataIOSet().getAssignmentsinfo());

                    taskDef.getGeneral().getName().setValue(t.getName());
                    BusinessRuleTaskExecutionSet executionSet = taskDef.getExecutionSet();
                    executionSet.getIsAsync().setValue(findMetaBoolean(extensionValues, "customAsync"));

                    for (FeatureMap.Entry entry : t.getAnyAttribute()) {
                        if (entry.getEStructuralFeature().getName().equals("ruleFlowGroup")) {
                            executionSet.getRuleFlowGroup().setValue(entry.getValue().toString());
                        }
                    }
                    setScriptProperties(t, executionSet);
                    return node;
                })
                .when(org.eclipse.bpmn2.ScriptTask.class, t ->
                        factoryManager.newNode(t.getId(), ScriptTask.class)
                )
                //.when(org.eclipse.bpmn2.ServiceTask.class,      t -> null)
                //.when(org.eclipse.bpmn2.ManualTask.class,       t -> null)
                .when(org.eclipse.bpmn2.UserTask.class, t -> {
                    Node<View<UserTask>, Edge> node = factoryManager.newNode(t.getId(), UserTask.class);
                    UserTaskExecutionSet executionSet = node.getContent().getDefinition().getExecutionSet();
                    AssignmentsInfoStringBuilder.setAssignmentsInfo(
                            task, executionSet.getAssignmentsinfo());

                    executionSet.getTaskName().setValue(t.getName());
                    executionSet.getIsAsync().setValue(findMetaBoolean(extensionValues, "customAsync"));
                    executionSet.getAdHocAutostart().setValue(findMetaBoolean(extensionValues, "customAutoStart"));

                    executionSet.getSubject().setValue(findInputValue(inputAssociations, "Comment"));
                    executionSet.getTaskName().setValue(findInputValue(inputAssociations, "TaskName"));
                    executionSet.getSkippable().setValue(findInputBooleans(inputAssociations, "Skippable"));
                    executionSet.getDescription().setValue(findInputValue(inputAssociations, "Description"));
                    executionSet.getPriority().setValue(findInputValue(inputAssociations, "Priority"));
                    executionSet.getCreatedBy().setValue(findInputValue(inputAssociations, "CreatedBy"));

                    executionSet.getActors().setValue(join(getActors(task)));
                    executionSet.getGroupid().setValue(findInputValue(inputAssociations, "GroupId"));

                    setScriptProperties(task, executionSet);
                    return node;
                })
                .orElse(t ->
                                factoryManager.newNode(t.getId(), NoneTask.class)
                )
                .apply(task)
                .value();
    }

    private String join(List<String> strings) {
        return strings.stream().collect(Collectors.joining(","));
    }

    private List<String> getActors(Task task) {
        // get the user task actors
        List<ResourceRole> roles = task.getResources();
        List<String> users = new ArrayList<>();
        for (ResourceRole role : roles) {
            if (role instanceof PotentialOwner) {
                FormalExpression fe = (FormalExpression) role.getResourceAssignmentExpression().getExpression();
                users.add(fe.getBody());
            }
        }
        return users;
    }

    private void setScriptProperties(Task task, ScriptableExecutionSet executionSet) {
        @SuppressWarnings("unchecked")
        List<OnEntryScriptType> onEntryExtensions =
                (List<OnEntryScriptType>) task.getExtensionValues().get(0).getValue()
                        .get(DroolsPackage.Literals.DOCUMENT_ROOT__ON_ENTRY_SCRIPT, true);
        @SuppressWarnings("unchecked")
        List<OnExitScriptType> onExitExtensions =
                (List<OnExitScriptType>) task.getExtensionValues().get(0).getValue()
                        .get(DroolsPackage.Literals.DOCUMENT_ROOT__ON_EXIT_SCRIPT, true);

        if (!onEntryExtensions.isEmpty()) {
            executionSet.setOnEntryAction(new OnEntryAction(onEntryExtensions.get(0).getScript()));
            executionSet.setScriptLanguage(new ScriptLanguage(extractScriptLanguage(onEntryExtensions.get(0).getScriptFormat())));
        }

        if (!onExitExtensions.isEmpty()) {
            executionSet.setOnExitAction(new OnExitAction(onExitExtensions.get(0).getScript()));
        }
    }

    private String extractScriptLanguage(String format) {
        switch (format) {
            case "http://www.java.com/java":
                return "java";
            case "http://www.mvel.org/2.0":
                return "mvel";
            case "http://www.javascript.com/javascript":
                return "javascript";
            default:
                return "java";
        }
    }

}