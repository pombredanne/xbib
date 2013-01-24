package org.xbib.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CoderResult;
import java.nio.charset.CharsetDecoder;
import java.text.Normalizer;

public abstract class SingleByteDecoder extends CharsetDecoder {

    private boolean composeCharactersAfterConversion = true;

    /**
     * @return Returns the composeCharactersAfterConversion.
     * @since 09.07.2008
     */
    public boolean isComposeCharactersAfterConversion() {
        return this.composeCharactersAfterConversion;
    }

    /**
     * @param composeCharactersAfterConversion
     *            The composeCharactersAfterConversion to set.
     * @since 09.07.2008
     */
    public void setComposeCharactersAfterConversion(
            boolean composeCharactersAfterConversion) {
        this.composeCharactersAfterConversion = composeCharactersAfterConversion;
    }

    protected SingleByteDecoder(Charset cs) {
        super(cs, 1.0f, 1.0f);
    }

    @Override
    protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
        ByteBuffer inputBuffer = ByteBuffer.allocate(30);
        while (in.hasRemaining()) {
            byte c = in.get();
            inputBuffer.put(c);

            StringBuilder convertedInputBuffer = null;
            if (!isCombiningCharacter(c)) {
                convertedInputBuffer = new StringBuilder();
                for (int i = inputBuffer.position() - 1; i >= 0; i--) {
                    char convertedCharacter = byteToChar(inputBuffer.get(i));
                    String convertedCharacterAsString;
                    if (convertedCharacter == 0) {
                        convertedCharacterAsString = replacement();
                    } else {
                        convertedCharacterAsString = String
                                .valueOf(convertedCharacter);
                    }

                    convertedInputBuffer.append(convertedCharacterAsString);
                }

                if (this.composeCharactersAfterConversion) {
                    convertedInputBuffer = new StringBuilder(Normalizer.normalize(convertedInputBuffer.toString(), Normalizer.Form.NFC));
                }
                
            }
            if (convertedInputBuffer != null) {
                if (out.remaining() < convertedInputBuffer.length()) {
                    /*
                     * output buffer is to small --> reset position and return
                     * OVERFLOW
                     */
                    in.position(in.position() - inputBuffer.position());
                    return CoderResult.OVERFLOW;
                }
                out.append(convertedInputBuffer);
                inputBuffer.clear();
            }

        }
        return CoderResult.UNDERFLOW;
    }

    public abstract boolean isCombiningCharacter(byte c);

    public abstract char byteToChar(byte b);

}
