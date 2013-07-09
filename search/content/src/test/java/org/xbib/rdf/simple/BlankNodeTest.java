package org.xbib.rdf.simple;

import java.util.Iterator;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.IdentifiableNode;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;

public class BlankNodeTest extends Assert {

    @Test
    public void testBlankNodeRenumbering() throws Exception {
        IdentifiableNode.reset();

        Resource r = new SimpleResource().id(IRI.create("urn:meta1"));

        // test order of adding
        Resource r1 = r.newResource("urn:res1");
        r1.add("urn:has", "a first resource");
        r.add("urn:has", "a first property");
        Resource q = new SimpleResource().id(IRI.create("urn:meta2"));
        Resource r2 = q.newResource("urn:res2");
        r2.add("urn:has", "a second resource");
        q.add("urn:has", "a second property");
        // we test here resource adding
        r.add("a:res", q);

        Iterator<Triple> it = r.propertyIterator();
        assertEquals(it.next().toString(), "urn:meta1 urn:res1 <genid:genid1>");
        assertEquals(it.next().toString(), "urn:meta1 urn:has a first property");
        assertEquals(it.next().toString(), "urn:meta1 a:res <urn:meta2>");
    }

    @Test
    public void testIterator() throws Exception {
        IdentifiableNode.reset();

        Resource r = new SimpleResource()
                .id(IRI.create("res1"));
        r.add("p0", "l0")
            .newResource("res2")
                .add("p1", "l1")
                .add("p2", "l2")
            .newResource("res3")
                .add("p1", "l1")
                .add("p2", "l2")
            .newResource("res4")
                .add("p1", "l1")
                .add("p2", "l2");

        Iterator<Triple> it = r.iterator();

        assertEquals(it.next().toString(), "res1 p0 l0");
        assertEquals(it.next().toString(), "res1 res2 <genid:genid1>");
        assertEquals(it.next().toString(), "_:genid1 p1 l1");
        assertEquals(it.next().toString(), "_:genid1 p2 l2");
        assertEquals(it.next().toString(), "_:genid1 res3 <genid:genid2>");
        assertEquals(it.next().toString(), "_:genid2 p1 l1");
        assertEquals(it.next().toString(), "_:genid2 p2 l2");
        assertEquals(it.next().toString(), "_:genid2 res4 <genid:genid3>");
        assertEquals(it.next().toString(), "_:genid3 p1 l1");
        assertEquals(it.next().toString(), "_:genid3 p2 l2");
    }

    @Test
    public void testResIterator() throws Exception {
        IdentifiableNode.reset();

        Resource r = new SimpleResource()
                .id(IRI.create("res0"));
        r.add("p0", "l0")
                .newResource("res")
                .add("p1", "l1")
                .add("p2", "l2")
                .newResource("res")
                .add("p1", "l1")
                .add("p2", "l2")
                .newResource("res")
                .add("p1", "l1")
                .add("p2", "l2");

        Iterator<Triple> it = r.iterator();

        assertEquals(it.next().toString(), "res0 p0 l0");
        assertEquals(it.next().toString(), "res0 res <genid:genid1>");
        assertEquals(it.next().toString(), "_:genid1 p1 l1");
        assertEquals(it.next().toString(), "_:genid1 p2 l2");
        assertEquals(it.next().toString(), "_:genid1 res <genid:genid2>");
        assertEquals(it.next().toString(), "_:genid2 p1 l1");
        assertEquals(it.next().toString(), "_:genid2 p2 l2");
        assertEquals(it.next().toString(), "_:genid2 res <genid:genid3>");
        assertEquals(it.next().toString(), "_:genid3 p1 l1");
        assertEquals(it.next().toString(), "_:genid3 p2 l2");
    }
}
