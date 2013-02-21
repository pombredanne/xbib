package org.xbib.rdf.simple;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;

public class SimpleTripleTest extends Assert {

    @Test
    public void testSimpleTripleEquaivalence() {
        SimpleTriple a = new SimpleTriple(IRI.create("urn:1"), IRI.create("urn:2"), new SimpleLiteral("Hello World"));
        SimpleTriple b = new SimpleTriple(IRI.create("urn:1"), IRI.create("urn:2"), new SimpleLiteral("Hello World"));
        assertEquals(a.toString(), b.toString());
    }
}
