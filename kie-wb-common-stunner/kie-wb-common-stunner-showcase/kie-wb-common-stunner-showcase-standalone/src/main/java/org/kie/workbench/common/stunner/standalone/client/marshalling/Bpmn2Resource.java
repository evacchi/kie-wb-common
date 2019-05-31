package org.kie.workbench.common.stunner.standalone.client.marshalling;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.XMLParser;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.util.XmlExtendedMetadata;

public class Bpmn2Resource extends XMLResourceImpl {
    static {
        EPackage.Registry packageRegistry = EPackage.Registry.INSTANCE;
        packageRegistry.put("http://www.omg.org/spec/BPMN/20100524/MODEL", Bpmn2Package.eINSTANCE);
//                packageRegistry.put("http://www.jboss.org/drools", DroolsPackage.eINSTANCE);
    }

    public void load(Node node) throws IOException {
        HashMap<Object, Object> options = new HashMap<>();
//                options.put(XMLResource.OPTION_DOM_USE_NAMESPACES_IN_SCOPE, true);
        options.put(XMLResource.OPTION_EXTENDED_META_DATA, new XmlExtendedMetadata());
        options.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, true);
        options.put(XMLResource.OPTION_DISABLE_NOTIFY, true);
        options.put(XMLResource.OPTION_PROCESS_DANGLING_HREF,
                    XMLResource.OPTION_PROCESS_DANGLING_HREF_RECORD);

        super.load(node, options);
    }

    public void load(String contents) throws IOException {
        XMLParser parser = GWT.create(XMLParser.class);
        Document doc = parser.parse(contents);
        load(doc);
    }
}
