package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bpsim.BPSimDataType;
import bpsim.BpsimPackage;
import bpsim.ControlParameters;
import bpsim.ElementParameters;
import bpsim.PriorityParameters;
import bpsim.ResourceParameters;
import bpsim.Scenario;
import bpsim.ScenarioParameters;
import bpsim.TimeParameters;
import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Documentation;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.LaneSet;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.Property;
import org.eclipse.bpmn2.Relationship;
import org.eclipse.bpmn2.SubProcess;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.eclipse.bpmn2.di.BPMNEdge;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.kie.workbench.common.stunner.bpmn.backend.converters.tasks.Simulations;
import org.kie.workbench.common.stunner.bpmn.backend.fromstunner.ElementContainer;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.DeclarationList;
import org.kie.workbench.common.stunner.bpmn.definition.property.simulation.SimulationSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessVariables;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;
import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpsim;
import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.di;

public class SubProcessPropertyWriter extends PropertyWriter implements ElementContainer {

    public static final String defaultRelationshipType = "BPSimData";

    private final SubProcess process;
    private Collection<ElementParameters> simulationParameters = new ArrayList<>();
    private Map<String, BasePropertyWriter> childElements = new HashMap<>();

    public SubProcessPropertyWriter(SubProcess process) {
        super(process);
        this.process = process;
    }

    public void addSimulationSet(SimulationSet simulations) {
        this.simulationParameters.add(Simulations.toElementParameters(simulations));
    }

    public Collection<ElementParameters> getSimulationParameters() {
        return simulationParameters;
    }

    public void addChildElement(BasePropertyWriter p) {
        this.childElements.put(p.getElement().getId(), p);
        if (p.getElement() instanceof FlowElement) {
            process.getFlowElements().add((FlowElement) p.getElement());
        }
        if (p instanceof ActivityPropertyWriter) {
            ElementParameters simulationParameters = ((ActivityPropertyWriter) p).getSimulationParameters();
            if (simulationParameters != null) {
                this.simulationParameters.add(simulationParameters);
            }
        }

        addAllBaseElements(p.getBaseElements());
    }

    public BasePropertyWriter getChildElement(String id) {
        return this.childElements.get(id);
    }

    @Override
    public void addChildEdge(BPMNEdge edge) {

    }

    public void addAllBaseElements(Collection<BaseElement> baseElements) {
        baseElements.forEach(el -> this.baseElements.put(el.getId(), el));
    }

//    public void setName(String value) {
//        process.setName(value);
//    }

    public void setDocumentation(String documentation) {
        Documentation d = bpmn2.createDocumentation();
        d.setText(asCData(documentation));
        process.getDocumentation().add(d);
    }

    public void setPackage(String value) {
        process.getAnyAttribute().add(
                Attributes.drools("packageName", String.valueOf(value)));
    }

    public void setVersion(String value) {
        process.getAnyAttribute().add(Attributes.drools("version", String.valueOf(value)));
    }

    public void setAdHoc(Boolean adHoc) {
        process.getAnyAttribute().add(Attributes.drools("adHoc", String.valueOf(adHoc)));
    }

    public void setDescription(String value) {
        setMeta("customDescription", value);
    }

    public void setSimulationSet(SimulationSet simulations) {
        this.simulationParameters.add(Simulations.toElementParameters(simulations));
    }

    // eww
    protected String asCData(String original) {
        return "<![CDATA[" + original + "]]>";
    }

    public void setProcessVariables(ProcessVariables processVariables) {
        String value = processVariables.getValue();
        DeclarationList declarationList = DeclarationList.fromString(value);
        declarationList.getDeclarations().forEach(decl -> {
            ItemDefinition typeDef = bpmn2.createItemDefinition();
            typeDef.setId("_" + decl.getIdentifier() + "Item");
            typeDef.setStructureRef(decl.getType());

            Property property = bpmn2.createProperty();
            property.setId(decl.getIdentifier());
            property.setItemSubjectRef(typeDef);

            process.getProperties().add(property);
            this.addBaseElement(typeDef);
        });
    }

    public void addLaneSet(List<LanePropertyWriter> lanes) {
        if (lanes.isEmpty()) {
            return;
        }
        LaneSet laneSet = bpmn2.createLaneSet();
        List<org.eclipse.bpmn2.Lane> laneList = laneSet.getLanes();
        lanes.forEach(l -> laneList.add(l.getElement()));
        process.getLaneSets().add(laneSet);
    }
}
