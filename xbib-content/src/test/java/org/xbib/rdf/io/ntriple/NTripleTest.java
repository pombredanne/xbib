
package org.xbib.rdf.io.ntriple;

import java.io.StringWriter;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleLiteral;
import org.xbib.rdf.simple.SimpleResource;

public class NTripleTest<S extends Identifier, P extends Property, O extends Node> {

    @Test
    public void testNTripleWrite() throws Exception {
        SimpleResource<S, P, O> m = createResource();
        StringWriter w = new StringWriter();
        NTripleWriter t = new NTripleWriter();
        t.write(m, w);
    }

    @Test
    public void testNTripleWriteInt() throws Exception {
        SimpleResource<S, P, O> resource = new SimpleResource();
        resource.id(IRI.create("urn:doc1"));
        resource.add(
                "http://purl.org/dc/elements/1.1/date",
                new SimpleLiteral("2010").type(IRI.create("http://www.w3.org/2001/XMLSchema#integer")));
        StringWriter w = new StringWriter();
        NTripleWriter t = new NTripleWriter();
        t.write(resource, w);
        Assert.assertEquals( w.toString(), "<urn:doc1> <http://purl.org/dc/elements/1.1/date> \"2010\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n");
    }

    private SimpleResource<S, P, O> createResource() {
        SimpleResource<S, P, O> m = new SimpleResource();
        String id = "urn:doc1";
        m.id(IRI.create(id));
        m.add("http://purl.org/dc/elements/1.1/creator", "Smith");
        m.add("http://purl.org/dc/elements/1.1/creator", "Jones");
        Resource r = m.newResource("dcterms:hasPart");
        r.add("http://purl.org/dc/elements/1.1/title", "This is a part");
        r.add("http://purl.org/dc/elements/1.1/title", "of a title");
        r.add("http://purl.org/dc/elements/1.1/creator", "Jörg Prante");
        r.add("http://purl.org/dc/elements/1.1/date", "2009");
        m.add("http://purl.org/dc/elements/1.1/title", "A sample title");
        r = m.newResource("http://purl.org/dc/terms/isPartOf");
        r.add("http://purl.org/dc/elements/1.1/title", "another");
        r.add("http://purl.org/dc/elements/1.1/title", "title");
        return m;
    }
}
