package org.xbib.re;

import java.io.FilterReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;


class Unescaper extends FilterReader {

    private PushbackReader push;
    private boolean escaped = false;

    public Unescaper(Reader r) {
        super(r);
        push = new PushbackReader(r, 3);
    }

    public boolean wasEscaped() {
        return escaped;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int read() throws IOException {
        int ch = push.read();
        try {
            escaped = false;
            if (ch != '\\') {
                return ch;
            }
            escaped = true;
            ch = push.read();
            switch ((char) ch) {
                case '0':
                case '1':
                case '2':
                case '3':
                    int ch2 = push.read();
                    int ch3 = push.read();
                    int build = ch * 64 - 06660;
                    if (ch2 >= '0' && ch2 <= '7') {
                        build += ch2 * 8;
                        if (ch3 >= '0' && ch3 <= '7') {
                            build += ch3;
                        } else {
                            push.unread(ch3);
                        }
                    } else {
                        push.unread(ch2);
                        push.unread(ch3);
                    }
                    return build;
                case 'n':
                    return '\n';
                case 'r':
                    return '\r';
                case 'b':
                    return '\b';
                case 't':
                    return '\t';
                case 'f':
                    return '\f';
                case 'x':
                    return Integer.parseInt("" + (char) push.read() + (char) push.read(), 16);
                case 'u':
                    return Integer.parseInt(""
                            + (char) push.read() + (char) push.read()
                            + (char) push.read() + (char) push.read(),
                            16);
                default:
                    return ch;
            }
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Illegal \\" + ch + " escape sequence");
        }
    }


}
