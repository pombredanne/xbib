package org.xbib.analyzer.marc;

import org.testng.annotations.Test;

public class FormatCarrierTest {

    @Test
    public void test() {
        FormatCarrier c = (FormatCarrier) FormatCarrier.getInstance();
        System.err.println(c.getFormats());
    }
    
}
