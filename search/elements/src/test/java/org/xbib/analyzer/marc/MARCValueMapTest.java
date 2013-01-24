package org.xbib.analyzer.marc;

import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MARCValueMapTest extends Assert {
    
    @Test
    public void testAssoc() throws IOException {
        MARCAssocValueMapper mapper = new MARCAssocValueMapper("sigel2isil");
        assertNotNull(mapper);
    }
    
    @Test
    public void testMap() throws IOException {
        MARCValueMapper mapper = new MARCValueMapper("product2isil");
        assertNotNull(mapper);
    }
    
}
