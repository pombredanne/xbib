
package org.xbib.rdf.io;

import org.xbib.rdf.io.xml.XMLResourceWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleResource;

public class SimpleTest <S extends Resource<S, P, O>, P extends Property, O extends Literal<O>> 
   extends Assert  {

    @Test
    public void testSerialization() throws Exception {
        SimpleResource<S, P, O> d1 = new SimpleResource<S, P, O>(IRI.create("urn:doc1"));
        d1.id(IRI.create("urn:doc1"));
        d1.property("urn:valueURI", "Hello World");
        Resource<S, P, O> resource = d1.newResource("urn:resource");
        resource.property("urn:property", "value");
        Resource<S, P, O> nestedResource = resource.newResource("urn:nestedresource");
        nestedResource.property("urn:nestedproperty", "nestedvalue");
        SimpleResource<S, P, O> d2;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(buffer);
        out.writeObject(d1);
        out.close();
        ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        d2 = (SimpleResource<S, P, O>) in.readObject();
        in.close();
        assertEquals(d1, d2);
    }    
    
    @Test
    public void testXMLResourceWriter() throws Exception {
        SimpleResource<S, P, O> root = new SimpleResource<S, P, O>(IRI.create("urn:root"));
        Resource resource = root.newResource("urn:resource");
        resource.property("urn:property", "value");
        Resource nestedResource = resource.newResource("urn:nestedresource");
        nestedResource.property("urn:nestedproperty", "nestedvalue");
        XMLResourceWriter w = new XMLResourceWriter();
        StringWriter sw = new StringWriter();
        w.toXML(root, sw);
        assertEquals("<urn:root><urn:resource><urn:property>value</urn:property><urn:nestedresource><urn:nestedproperty>nestedvalue</urn:nestedproperty></urn:nestedresource></urn:resource></urn:root>", sw.toString());
    }

}
