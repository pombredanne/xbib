package org.xbib.re;

import java.io.Reader;

class CharSequenceReader extends Reader {
    private final CharSequence buf;
    private int count;
    private int pos;

    public CharSequenceReader(CharSequence buf) {
        this(buf, 0, buf.length());
    }

    public CharSequenceReader(CharSequence buf, int offset, int length) {
        this.buf = buf;
        count = offset + length;
        pos = offset;
    }

    public int read(char[] cbuf, int off, int len) {
        len = Math.min(len, count - pos);
        if (len == 0) {
            return -1;
        }
        int end = off + len;
        while (off < end) {
            cbuf[off++] = buf.charAt(pos++);
        }
        return len;
    }

    public void close() {
    }
}
