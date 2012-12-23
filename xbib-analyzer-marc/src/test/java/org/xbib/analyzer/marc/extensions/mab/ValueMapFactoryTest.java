package org.xbib.analyzer.marc.extensions.mab;

import java.io.IOException;
import java.util.Map;
import org.testng.annotations.Test;
import org.xbib.elements.ValueMapFactory;

public class ValueMapFactoryTest {
    
    @Test
    public void testAssoc() throws IOException {
        Map<String,String> map = ValueMapFactory.getAssocStringMap("sigel2isil");
    }
    
    @Test
    public void testMap() throws IOException {
        Map map = ValueMapFactory.getMap("product2isil");
    }
    
}
