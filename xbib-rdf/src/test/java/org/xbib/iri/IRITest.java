package org.xbib.iri;

import org.testng.Assert;
import org.testng.annotations.Test;

public class IRITest extends Assert {

    @Test
    public void testIRI() {
        IRI iri = IRI.create("@context");
        assertEquals(null, iri.getScheme());
        assertEquals("@context",iri.getSchemeSpecificPart());
    }
}
