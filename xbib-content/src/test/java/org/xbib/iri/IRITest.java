package org.xbib.iri;

import org.testng.Assert;
import org.testng.annotations.Test;

public class IRITest extends Assert {

    @Test
    public void testJsonLd() {
        IRI iri = IRI.create("@context");
        assertEquals(null, iri.getScheme());
        assertEquals("@context",iri.getSchemeSpecificPart());
    }

    @Test(expectedExceptions = org.xbib.iri.IRISyntaxException.class)
    public void testIllegalBlankNodeIRI() {
        IRI iri = IRI.create("_:a1");
        assertEquals("_", iri.getScheme());
        assertEquals("a1",iri.getSchemeSpecificPart());
    }

}
