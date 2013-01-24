package org.xbib.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.text.Normalizer;

public abstract class SingleByteEncoder extends CharsetEncoder {

    private boolean decomposeCharactersBeforeConversion = true;

    /**
     * @return Returns the decomposeCharactersBeforeConversion.
     * @since 09.07.2008
     */
    public boolean isDecomposeCharactersBeforeConversion() {
        return this.decomposeCharactersBeforeConversion;
    }

    /**
     * @param decomposeCharactersBeforeConversion
     *            The decomposeCharactersBeforeConversion to set.
     * @since 09.07.2008
     */
    public void setDecomposeCharactersBeforeConversion(
            boolean decomposeCharactersBeforeConversion) {
        this.decomposeCharactersBeforeConversion = decomposeCharactersBeforeConversion;
    }

    protected SingleByteEncoder(Charset cs) {
        super(cs, 1.0f, 1.0f);
    }

    /**
     * @see java.nio.charset.CharsetEncoder#encodeLoop(java.nio.CharBuffer,
     *      java.nio.ByteBuffer)
     * @since 07.07.2008
     */
    @Override
    protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
        CharBuffer inputBuffer = CharBuffer.allocate(30);
        while (in.hasRemaining()) {
            char c = in.get();
            String charAsString;
            if (isDecomposeCharactersBeforeConversion()) {
                charAsString = Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD);
            } else {
                charAsString = String.valueOf(c);
            }
            if (out.remaining() < inputBuffer.position()
                    + charAsString.length()) {
                /*
                 * output buffer is to small --> reset position and return
                 * OVERFLOW
                 */
                in.position(in.position() - inputBuffer.position() - 1);
                return CoderResult.OVERFLOW;
            }

            if (inputBuffer.position() > 0 && !isCombiningCharacter(c)) {
                /*
                 * convert characters in reverse order
                 */
                for (int i = inputBuffer.position() - 1; i >= 0; i--) {
                    convert(inputBuffer.get(i), out);
                }
                inputBuffer.clear();
            }
            inputBuffer.append(charAsString);
        }

        if (inputBuffer.position() == 1) {
            convert(inputBuffer.get(), out);
        } else if (inputBuffer.position() > 1) {
            for (int i = inputBuffer.position() - 1; i >= 0; i--) {
                convert(inputBuffer.get(i), out);
            }
        }

        return CoderResult.UNDERFLOW;
    }

    public abstract byte charToByte(char c);

    public boolean isCombiningCharacter(char c) {
        return c >= '\u0300' && c <= '\u036F';
    }

    private void convert(char c, ByteBuffer out) {
        byte c_ = charToByte(c);
        if (c_ != 0) {
            out.put(c_);
        } else {
            out.put(replacement());
        }
    }
}
