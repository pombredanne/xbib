package org.xbib.re;

class Sub {

    final int sp;

    final int ep;

    Object label;

    public Sub(int sp, int ep) {
        this.sp = sp;
        this.ep = ep;
    }

    @Override
    public int hashCode() {
        return sp << 16 | ep;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Sub)) {
            return false;
        }
        Sub sub = (Sub) o;
        return sp == sub.sp && ep == sub.ep;
    }

    @Override
    public String toString() {
        return "{" + sp + "," + ep + "}";
    }

    public Sub setLabel(Object label) {
        this.label = label;
        return this;
    }

    public Object getLabel() {
        return label;
    }

}
