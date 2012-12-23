
package org.xbib.rdf.io.turtle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleResource;

public class TurtleTest<S extends Resource<S, P, O>, P extends Property, O extends Literal<O>> extends Assert {

    @Test
    public void testTurtleDemoReader() throws Exception {
        StringBuilder sb = new StringBuilder();
        String filename = "/org/xbib/rdf/io/turtle-demo.ttl";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        String s1 = sb.toString().trim();
        //logger.log(Level.INFO, "turtle content from file = {0}", s1.length());
        SimpleResource<S, P, O> resource = createResource();
        StringWriter sw = new StringWriter();
        TurtleWriter t = new TurtleWriter();
        t.write(resource, true, sw);
        String s2 = sw.toString().trim();
        //logger.log(Level.INFO, "turtle content = {0}", s2);
        //assertEquals(s2, s1);
    }

    private SimpleResource createResource() {
        SimpleResource<S, P, O> resource = new SimpleResource();
        String id = "urn:doc1";
        resource.id(IRI.create(id));
        resource.property("dc:creator", "Smith");
        resource.property("dc:creator", "Jones");
        Resource r = resource.newResource("dcterms:hasPart");
        r.property("dc:title", "This is a part");
        r.property("dc:title", "of the sample title");
        r.property("dc:creator", "Jörg Prante");
        r.property("dc:date", "2009");
        resource.property("dc:title", "A sample title");
        r = resource.newResource("dcterms:isPartOf");
        r.property("dc:title", "another");
        r.property("dc:title", "title");
        return resource;
    }
    
    
    @Test
    public void testTurtleReadWrite() throws Exception {
        Resource resource = createResource2();
        StringWriter sw = new StringWriter();
        TurtleWriter t = new TurtleWriter();
        t.write(resource, true, sw);
        String s2 = sw.toString().trim();
       // logger.log(Level.INFO, "turtle content = {0}", s2);
    }
    
    
    private Resource<S,P,O> createResource2() {
        Resource<S,P,O> resource = new SimpleResource();
        resource.id(IRI.create("urn:resource"));
        resource.property("dc:title", "Hello");
        resource.property("dc:title", "World");
        resource.property("xbib:person", "Jörg Prante");
        resource.property("dc:subject", "An");
        resource.property("dc:subject", "example");
        resource.property("dc:subject", "for");
        resource.property("dc:subject", "a");
        resource.property("dc:subject", "sequence");
        resource.property("http://purl.org/dc/terms/place", "Köln");
        // sequence optimized for turtle output
        Resource<S,P,O> r1 = resource.newResource("urn:res1");
        r1.property("property1", "value1");
        r1.property("property2", "value2");
        Resource<S,P,O> r2 = resource.newResource("urn:res2");
        r2.property("property3", "value3");
        r2.property("property4", "value4");
        Resource<S,P,O> r3 = resource.newResource("urn:res3");
        r3.property("property5", "value5");
        r3.property("property6", "value6");
        return resource;
    }
}
