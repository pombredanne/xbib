package org.xbib.charset;

import java.text.Normalizer;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NormalizerTest extends Assert {

    @Test
    public void testNormalizer() throws Exception {
        byte[] b = new byte[]{(byte)103, (byte)101, (byte)109, (byte)97, (byte) 204, (byte) 136, (byte) 195, (byte) 159};
        String input = new String(b, "UTF-8");
        String norm = Normalizer.normalize(input, Normalizer.Form.NFC);
        assertEquals("gemäß",norm);
    }

    @Test
    public void tesNFC() {
        String s = "Für Bandanzeige bitte zugehörige Publikationen anklicken";
        System.err.println("l="+ s.length());
        String norm = Normalizer.normalize(s, Normalizer.Form.NFC);
        System.err.println("norm="+ norm.length());
    }
    
}
