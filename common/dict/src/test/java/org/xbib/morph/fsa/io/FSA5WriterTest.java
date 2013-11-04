package org.xbib.morph.fsa.io;


import org.xbib.morph.fsa.io.FSA5Codec;
import org.xbib.morph.fsa.io.WriterTestBase;

public class FSA5WriterTest extends WriterTestBase {
    protected FSA5Codec createCodec() {
        return new FSA5Codec();
    }
}
