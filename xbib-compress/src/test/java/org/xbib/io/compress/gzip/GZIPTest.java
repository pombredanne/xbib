
package org.xbib.io.compress.gzip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

public class GZIPTest {

    @Test
    public void testHelloWorld() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream zOut = new GZIPOutputStream(out)) {
            ObjectOutputStream objOut = new ObjectOutputStream(zOut);
            String helloWorld = "Hello World!";
            objOut.writeObject(helloWorld);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        GZIPInputStream zIn = new GZIPInputStream(in);
        ObjectInputStream objIn = new ObjectInputStream(zIn);
        assertEquals("Hello World!", objIn.readObject());
    }

}
