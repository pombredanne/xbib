package org.xbib.rdf.io.xml;

import java.io.StringWriter;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.IdentifiableNode;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleResource;

public class XmlResourceWriterTest<S extends Identifier, P extends Property, O extends Node> extends Assert {
    
    @Test
    public void testXMLResourceWriter() throws Exception {
        IdentifiableNode.reset();
        Resource<S, P, O> root = new SimpleResource().id(IRI.create("urn:root"));
        Resource resource = root.newResource("urn:resource");
        resource.add("urn:property", "value");
        Resource nestedResource = resource.newResource("urn:nestedresource");
        nestedResource.add("urn:nestedproperty", "nestedvalue");
        XmlResourceWriter w = new XmlResourceWriter();
        StringWriter sw = new StringWriter();
        w.toXML(root, sw);
        assertEquals("<urn:root><urn:resource><urn:property>value</urn:property><urn:nestedresource><urn:nestedproperty>nestedvalue</urn:nestedproperty></urn:nestedresource></urn:resource></urn:root>", sw.toString());
    }    
    
    @Test
    public void testResourceXml() throws Exception {
        IdentifiableNode.reset();
        SimpleResource<S, P, O> parent = new SimpleResource();
        parent.id(IRI.create("urn:doc3"));
        Resource<S, P, O> child = parent.newResource("urn:resource");
        child.add("urn:property", "value");
        XmlResourceWriter xmlrw = new XmlResourceWriter();
        // parent parent
        StringWriter sw = new StringWriter();
        xmlrw.toXML(parent, sw);
        assertEquals("<urn:doc3><urn:resource><urn:property>value</urn:property></urn:resource></urn:doc3>", sw.toString());
        // child parent
        sw = new StringWriter();
        xmlrw.toXML(child, sw);
        assertEquals("<genid:genid1><urn:property>value</urn:property></genid:genid1>", sw.toString());
    }
}
