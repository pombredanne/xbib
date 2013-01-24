
package org.xbib.rdf.io;

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
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.io.xml.XmlResourceWriter;
import org.xbib.rdf.simple.SerializableResource;
import org.xbib.rdf.simple.SimpleResource;

public class SerializationTest <S extends Identifier, P extends Property, O extends Literal<O>>
   extends Assert  {

    @Test
    public void testSerialization() throws Exception {
        Resource<S, P, O> d1 = new SerializableResource();
        d1.id(IRI.create("urn:doc1"));
        d1.add("urn:valueURI", "Hello World");
        Resource<S, P, O> resource = d1.newResource("urn:resource");
        resource.add("urn:property", "value");
        Resource<S, P, O> nestedResource = resource.newResource("urn:nestedresource");
        nestedResource.add("urn:nestedproperty", "nestedvalue");
        Resource<S, P, O> d2;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(buffer);
        out.writeObject(d1);
        out.close();
        ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        d2 = (SerializableResource<S, P, O>) in.readObject();
        in.close();
        assertEquals(d1, d2);
    }

}
