package org.xbib.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class EntitiesTest extends Assert {

    @Test
    public void testEntities() throws  Exception {
       String s = "&Ouml;sten Bj&ouml;rnberg";
        s = Entities.HTML40.unescape(s);
        assertEquals(s, "Östen Björnberg");
    }

}
