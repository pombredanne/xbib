package org.xbib.elements;

import java.io.IOException;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ValueMapFactoryTest extends Assert {
    
    @Test
    public void testAssoc() throws IOException {
        Map<String,String> map = ValueMapFactory.getAssocStringMap("/org/xbib/analyzer/elements/", "sigel2isil");
        assertNotNull(map);
    }
    
    @Test
    public void testMap() throws IOException {
        Map map = ValueMapFactory.getMap("/org/xbib/analyzer/elements/", "product2isil");
        assertNotNull(map);
    }
    
}
