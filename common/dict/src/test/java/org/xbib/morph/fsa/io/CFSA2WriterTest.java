package org.xbib.morph.fsa.io;

import org.testng.annotations.Test;
import org.xbib.morph.fsa.CFSA2;
import org.xbib.morph.fsa.io.FSA5Codec;
import org.xbib.morph.fsa.io.WriterTestBase;

/**
 *
 */
public class CFSA2WriterTest extends WriterTestBase {


    protected FSA5Codec createCodec() {
        return new FSA5Codec();
    }

    @Test
    public void testVIntCoding() {
        byte[] scratch = new byte[5];

        int[] values = {0, 1, 128, 256, 0x1000, Integer.MAX_VALUE};

        for (int v : values) {
            int len = CFSA2.writeVInt(scratch, 0, v);
            assertEquals(v, CFSA2.readVInt(scratch, 0));
            assertEquals(len, CFSA2.vIntLength(v));
        }
    }
}
