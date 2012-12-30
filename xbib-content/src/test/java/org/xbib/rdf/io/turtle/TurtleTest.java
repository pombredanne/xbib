package org.xbib.rdf.io.turtle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleResource;

public class TurtleTest<S extends Identifier, P extends Property, O extends Node>
        extends Assert {

    @Test
    public void testTurtleDemoReader() throws Exception {
        StringBuilder sb = new StringBuilder();
        String filename = "turtle-demo.ttl";
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
        Resource<S, P, O> resource = createResource();
        StringWriter sw = new StringWriter();
        TurtleWriter t = new TurtleWriter();
        t.write(resource, true, sw);
        String s2 = sw.toString().trim();
        assertEquals(s2, s1);
    }

    private Resource createResource() {
        Resource<S, P, O> resource = new SimpleResource()
                .id(IRI.create("urn:doc1"));
        resource.add("dc:creator", "Smith");
        resource.add("dc:creator", "Jones");
        Resource r = resource.newResource("dcterms:hasPart")
                .add("dc:title", "This is a part")
                .add("dc:title", "of the sample title")
                .add("dc:creator", "Jörg Prante")
                .add("dc:date", "2009");
        resource.add("dc:title", "A sample title");
        r = resource.newResource("dcterms:isPartOf")
                .add("dc:title", "another")
                .add("dc:title", "title");
        return resource;
    }

    @Test
    public void testTurtleReadWrite() throws Exception {
        Resource resource = createResource2();
        StringWriter sw = new StringWriter();
        TurtleWriter t = new TurtleWriter();
        t.write(resource, true, sw);
        String s2 = sw.toString().trim();
    }

    private Resource<S, P, O> createResource2() {
        Resource<S, P, O> r = new SimpleResource()
                .id(IRI.create("urn:resource"))
                .add("dc:title", "Hello")
                .add("dc:title", "World")
                .add("xbib:person", "Jörg Prante")
                .add("dc:subject", "An")
                .add("dc:subject", "example")
                .add("dc:subject", "for")
                .add("dc:subject", "a")
                .add("dc:subject", "sequence")
                .add("http://purl.org/dc/terms/place", "Köln");
        // sequence optimized for turtle output
        Resource<S, P, O> r1 = r.newResource("urn:res1")
                .add("property1", "value1")
                .add("property2", "value2");
        Resource<S, P, O> r2 = r.newResource("urn:res2")
                .add("property3", "value3")
                .add("property4", "value4");
        Resource<S, P, O> r3 = r.newResource("urn:res3")
                .add("property5", "value5")
                .add("property6", "value6");
        return r;
    }
}
