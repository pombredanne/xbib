package org.xbib.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ILLNumberTest extends Assert {

    @Test
    public void testILLNumber() throws Exception {
        ILL ill = new ILL("DE-605-2012-301-0-6");
        String s = ill.getStandardNumberValue();
        assertEquals(s,"DE-605-2012-301-0-6");
        s = ill.getStandardNumberPrintableRepresentation();
        assertEquals(s,"DE-605-2012-301-0-6");
        boolean valid = ill.isValid();
        assertTrue(valid);
    }

    @Test
    public void testIncompleteILLNumber() throws Exception {
        ILL ill = new ILL("DE-605-2012-301-0");
        String s = ill.getStandardNumberValue();
        assertEquals(s,"DE-605-2012-301-0-6");
        s = ill.getStandardNumberPrintableRepresentation();
        assertEquals(s,"DE-605-2012-301-0-6");
        boolean valid = ill.isValid();
        assertTrue(valid);
    }

    @Test
    public void testILLNumber2() throws Exception {
        ILL ill = new ILL("DE-605-2012-2324580-0");
        String s = ill.getStandardNumberValue();
        assertEquals(s,"DE-605-2012-2324580-0-6");
        s = ill.getStandardNumberPrintableRepresentation();
        assertEquals(s,"DE-605-2012-2324580-0-6");
        boolean valid = ill.isValid();
        assertTrue(valid);
    }    

}
