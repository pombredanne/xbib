package org.xbib.analyzer.marc;

import java.io.IOException;
import org.testng.annotations.Test;

public class MARCValueMapTest {
    
    @Test
    public void testAssoc() throws IOException {
        MARCAssocValueMapper mapper = new MARCAssocValueMapper("sigel2isil");
    }
    
    @Test
    public void testMap() throws IOException {
        MARCValueMapper mapper = new MARCValueMapper("product2isil");
    }
    
}
