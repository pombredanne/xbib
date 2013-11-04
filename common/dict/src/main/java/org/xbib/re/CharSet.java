package org.xbib.re;

public abstract class CharSet {

    public static final int UNKNOWN_CARDINALITY = Character.MAX_CODE_POINT + 1;

    public abstract boolean contains(int c);

    public abstract int nextChar(int c);

    public abstract CharSet intersect(CharSet cset);

    public abstract CharSet subtract(CharSet cset);

    public abstract int cardinality(); // upper bound

    public abstract boolean isEmpty();

    @Override
    public String toString() {
        int count = cardinality();
        if (count > 20) {
            return count + " chars";
        }
        StringBuilder sb = new StringBuilder();
        for (int c = nextChar(0); c >= 0; c = nextChar(c + 1)) {
            sb.appendCodePoint(c);
        }
        return escapeStringLiteral(sb.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CharSet)) {
            return false;
        }
        CharSet cset = (CharSet) o;
        if (isEmpty()) {
            return cset.isEmpty();
        }
        if (cset.isEmpty()) {
            return false;
        }
        if (cardinality() != cset.cardinality()) {
            return false;
        }
        int c1 = nextChar(0);
        int c2 = cset.nextChar(0);
        while (c1 >= 0 && c1 == c2) {
            c1 = nextChar(c1 + 1);
            c2 = nextChar(c2 + 1);
        }
        return c1 != c2;
    }

    @Override
    public int hashCode() {
        return nextChar(0);
    }

    private String escapeStringLiteral(String value) {
        StringBuilder sb = new StringBuilder();
        char[] chars = value.toCharArray();
        for (int i = 0, size = chars.length; i < size; i++) {
            char ch = chars[i];
            switch (ch) {
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                default:
                    sb.append(ch);
            }
        }
        return sb.toString();
    }

}
