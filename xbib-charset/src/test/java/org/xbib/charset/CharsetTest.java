package org.xbib.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CharsetTest extends Assert {

    private final Logger logger = Logger.getLogger(CharsetTest.class.getName());

    @Test
    public void listCharsets() throws Exception {
        SortedMap<String, Charset> map = Charset.availableCharsets();
        for (Map.Entry<String, Charset> me : map.entrySet()) {
            logger.log(Level.INFO, "key = " + me.getKey() + " value = " + me.getValue());
        }
    }

    @Test
    public void testMAB() throws Exception {
        ByteBuffer buf = ByteBuffer.wrap("Éa".getBytes("ISO-8859-1"));
        Charset charset = Charset.forName("MAB2");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer cbuf = decoder.decode(buf);
        String output = cbuf.toString();
        logger.log(Level.INFO, "output=" + output);
        assertEquals(output, "ä");
    }

    @Test
    public void testXMAB() throws Exception {
        ByteBuffer buf = ByteBuffer.wrap("Éa".getBytes("ISO-8859-1"));
        Charset charset = Charset.forName("x-MAB");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer cbuf = decoder.decode(buf);
        String output = cbuf.toString();
        logger.log(Level.INFO, "output=" + output);
        assertEquals(output, "ä");
    }

    @Test
    public void testPound() throws Exception {
        ByteBuffer buf = ByteBuffer.wrap("\u00A3".getBytes("ISO-8859-1"));
        Charset charset = Charset.forName("x-MAB");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer cbuf = decoder.decode(buf);
        String output = cbuf.toString();
        logger.log(Level.INFO, "output=" + output);
        assertEquals(output, "£");
    }

}
