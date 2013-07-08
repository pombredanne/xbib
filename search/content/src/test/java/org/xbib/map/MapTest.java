package org.xbib.map;

import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class MapTest {

    @Test
    public void testSimple() {
        Map m1 = new HashMap();
        m1.put("Hello", "World");
        Map m2 = new HashMap();
        m2.put("key", m1);

        MapBasedAnyObject o = new MapBasedAnyObject(m2);
        o.get("key");
        o.getAnyObject("key");
        o.get("key.hello");
    }
}
