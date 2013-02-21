package org.xbib.rdf.simple;

import java.util.Iterator;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
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
}
