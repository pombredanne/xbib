package org.xbib.elements.mab;

import org.testng.annotations.Test;

public class FormatCarrierTest {

    @Test
    public void test() {
        FormatCarrier c = (FormatCarrier) FormatCarrier.getInstance();
        System.err.println(c.getFormats());
    }
    
}
