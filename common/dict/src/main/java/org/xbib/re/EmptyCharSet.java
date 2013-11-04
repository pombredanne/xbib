package org.xbib.re;

public final class EmptyCharSet extends CharSet {

    public static final EmptyCharSet INSTANCE = new EmptyCharSet();

    public boolean contains(int c) {
        return false;
    }

    public int nextChar(int c) {
        return -1;
    }

    public boolean isEmpty() {
        return true;
    }

    public int cardinality() {
        return 0;
    }

    public CharSet intersect(CharSet cset) {
        return this;
    }

    public CharSet subtract(CharSet cset) {
        return this;
    }
}
