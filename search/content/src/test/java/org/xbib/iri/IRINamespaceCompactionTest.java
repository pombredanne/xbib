
package org.xbib.iri;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.context.IRINamespaceContext;

public class IRINamespaceCompactionTest extends Assert {

    @Test
    public void testCompaction() throws Exception {
        IRINamespaceContext context = IRINamespaceContext.getInstance();
        assertEquals("http://purl.org/dc/elements/1.1/", context.getNamespaceURI("dc"));
        assertEquals("dc", context.getPrefix("http://purl.org/dc/elements/1.1/"));
        IRI dc = IRI.create("http://purl.org/dc/elements/1.1/creator");
        assertEquals(context.compact(dc).toString(), "dc:creator");
    }

}
