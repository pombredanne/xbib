package org.xbib.rdf.simple;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;

public class SimpleStatementTest extends Assert {

    @Test
    public void testStatementEquaivalence() {
        SimpleStatement a = new SimpleStatement("urn:1", IRI.create("urn:2"), new SimpleLiteral("Hello World"));
        SimpleStatement b = new SimpleStatement(IRI.create("urn:1"), IRI.create("urn:2"), new SimpleLiteral("Hello World"));
        assertEquals(a.toString(), b.toString());
    }
}
