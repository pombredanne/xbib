package org.xbib.rdf.simple;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.Loggers;

public class SimpleLiteralTest extends Assert {

    private final Logger logger = Loggers.getLogger(SimpleLiteralTest.class);

    @Test
    public void testLiteral() {
        SimpleLiteral<String> l = new SimpleLiteral<String>()
                .object("2013")
                .type(IRI.create("xsd:gYear"));
        assertEquals(l.toString(), "2013^^xsd:gYear");
        assertEquals(l.nativeValue(), 2013);
    }
}
