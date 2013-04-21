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
    //@Test
    public void testIllegalBlankNodeIRI() {
        IRI iri = IRI.create("_:a1");
        assertEquals("_", iri.getScheme());
        assertEquals("a1",iri.getSchemeSpecificPart());
    }

    @Test
    public void testRoutingByIRI() {
        IRI iri = IRI.create("http://index?type#id");
        assertEquals("http", iri.getScheme());
        assertEquals("index", iri.getHost());
        assertEquals("type", iri.getQuery());
        assertEquals("id", iri.getFragment());
    }

    @Test
    public void testCuri() {
        IRI curi = IRI.builder().curi("dc:creator").build();
        assertEquals("dc", curi.getScheme());
        assertEquals("creator", curi.getPath());
        curi = IRI.builder().curi("creator").build();
        assertNull(curi.getScheme());
        assertEquals("creator", curi.getPath());
    }

    @Test
    public void testSchemeSpecificPart() {
        IRI curi = IRI.builder().curi("dc:creator").build();
        assertEquals("dc", curi.getScheme());
        assertEquals("creator", curi.getSchemeSpecificPart());
        assertEquals("dc:creator", curi.toString());
        curi = IRI.builder().curi("creator").build();
        assertNull(curi.getScheme());
        assertEquals("creator", curi.getSchemeSpecificPart());
        assertEquals("creator", curi.toString());
    }
}
