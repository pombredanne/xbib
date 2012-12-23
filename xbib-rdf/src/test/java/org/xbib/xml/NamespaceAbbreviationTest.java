
package org.xbib.xml;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;

public class NamespaceAbbreviationTest extends Assert {

    @Test
    public void testCompaction() throws Exception {
        XMLNamespaceContext context = XMLNamespaceContext.getInstance();
        assertEquals("http://purl.org/dc/elements/1.1/", context.getNamespaceURI("dc"));
        assertEquals("dc", context.getPrefix("http://purl.org/dc/elements/1.1/"));
        IRI dc = IRI.create("http://purl.org/dc/elements/1.1/creator");
        assertEquals("dc:creator", context.compact(dc).toString());
    }

}
