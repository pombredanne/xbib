package org.xbib.re;

public final class SingleCharSet extends CharSet {

    private final int c;

    public SingleCharSet(int c) {
        this.c = c;
    }

    @Override
    public boolean contains(int c) {
        return c == this.c;
    }

    @Override
    public int nextChar(int c) {
        return (c <= this.c) ? this.c : -1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int cardinality() {
        return 1;
    }

    @Override
    public CharSet intersect(CharSet cset) {
        return cset.contains(c) ? this : EmptyCharSet.INSTANCE;
    }

    @Override
    public CharSet subtract(CharSet cset) {
        return cset.contains(c) ? EmptyCharSet.INSTANCE : this;
    }
}
