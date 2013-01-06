package org.xbib.io;

import java.io.Writer;

public class NullWriter extends Writer {

    public NullWriter() {
        super();
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }

    @Override
    public void write(char[] cbuf) {
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
    }

    @Override
    public void write(int c) {
    }

    @Override
    public void write(String str) {
    }

    @Override
    public void write(String str, int off, int len) {
    }
}
