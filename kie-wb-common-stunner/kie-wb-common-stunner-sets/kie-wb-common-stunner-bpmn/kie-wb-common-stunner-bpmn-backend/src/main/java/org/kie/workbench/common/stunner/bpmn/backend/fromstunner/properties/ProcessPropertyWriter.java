package org.kie.workbench.common.stunner.bpmn.backend.fromstunner.properties;

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
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Documentation;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.LaneSet;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.Property;
import org.eclipse.bpmn2.Relationship;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.eclipse.bpmn2.di.BPMNEdge;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EStructuralFeatureImpl;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.kie.workbench.common.stunner.bpmn.definition.SequenceFlow;
import org.kie.workbench.common.stunner.bpmn.definition.property.dataio.DeclarationList;
import org.kie.workbench.common.stunner.bpmn.definition.property.simulation.SimulationSet;
import org.kie.workbench.common.stunner.bpmn.definition.property.variables.ProcessVariables;

import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpmn2;
import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.bpsim;
import static org.kie.workbench.common.stunner.bpmn.backend.fromstunner.Factories.di;

public class ProcessPropertyWriter extends BasePropertyWriter {

    public static final String defaultRelationshipType = "BPSimData";

    private final Process process;
    private final BPMNDiagram bpmnDiagram;
    private Relationship relationship;
    private Map<String, BasePropertyWriter> childElements = new HashMap<>();

    public ProcessPropertyWriter(Process process) {
        super(process);
        this.process = process;

        this.bpmnDiagram = di.createBPMNDiagram();
        bpmnDiagram.setId(process.getId());

        BPMNPlane bpmnPlane = di.createBPMNPlane();
        bpmnDiagram.setPlane(bpmnPlane);
    }

    public Process getProcess() {
        return process;
    }

    public Relationship getRelationship() {
        return relationship;
    }

    public void addChildShape(BPMNShape shape) {
        if (shape != null) {
            bpmnDiagram.getPlane().getPlaneElement().add(shape);
        }
    }

    public void addChildEdge(BPMNEdge edge) {
        bpmnDiagram.getPlane().getPlaneElement().add(edge);
    }

    public BPMNDiagram getBpmnDiagram() {
        return bpmnDiagram;
    }

    public void addChildElement(BasePropertyWriter p) {
        this.childElements.put(p.getElement().getId(), p);
        if (p.getElement() instanceof FlowElement) {
            process.getFlowElements().add((FlowElement) p.getElement());
        }
        if (p instanceof SequenceFlowPropertyWriter) {
            addChildEdge(((SequenceFlowPropertyWriter) p).getEdge());
        }
        addChildShape(p.getShape());
        addAllBaseElements(p.getBaseElements());
    }

    public BasePropertyWriter getChildElement(String id) {
        return this.childElements.get(id);
    }

    public void addAllBaseElements(Collection<BaseElement> baseElements) {
        baseElements.forEach(el -> this.baseElements.put(el.getId(), el));
    }

    public void setName(String value) {
        process.setName(value);
    }

    public void setExecutable(Boolean value) {
        process.setIsExecutable(value);
    }

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
        Relationship relationship = bpmn2.createRelationship();
        relationship.setType(defaultRelationshipType);
        BPSimDataType simDataType = bpsim.createBPSimDataType();
        // currently support single scenario
        Scenario defaultScenario = bpsim.createScenario();
        ScenarioParameters scenarioParameters = bpsim.createScenarioParameters();
        defaultScenario.setId("default"); // single scenario suppoert
        defaultScenario.setName("Simulationscenario"); // single scenario support
        defaultScenario.setScenarioParameters(scenarioParameters);
        simDataType.getScenario().add(defaultScenario);
        ExtensionAttributeValue extensionElement = bpmn2.createExtensionAttributeValue();
        relationship.getExtensionValues().add(extensionElement);
        FeatureMap.Entry extensionElementEntry = new EStructuralFeatureImpl.SimpleFeatureMapEntry(
                (EStructuralFeature.Internal) BpsimPackage.Literals.DOCUMENT_ROOT__BP_SIM_DATA,
                simDataType);
        relationship.getExtensionValues().get(0).getValue().add(extensionElementEntry);

        ElementParameters elementParameters = bpsim.createElementParameters();

        ControlParameters controlParameters = bpsim.createControlParameters();
        PriorityParameters priorityParameters = bpsim.createPriorityParameters();
        ResourceParameters resourceParameters = bpsim.createResourceParameters();
        TimeParameters timeParameters = bpsim.createTimeParameters();

        elementParameters.setControlParameters(controlParameters);
        elementParameters.setPriorityParameters(priorityParameters);
        elementParameters.setResourceParameters(resourceParameters);
        elementParameters.setTimeParameters(timeParameters);

        defaultScenario.getElementParameters().add(elementParameters);

        this.relationship = relationship;
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
