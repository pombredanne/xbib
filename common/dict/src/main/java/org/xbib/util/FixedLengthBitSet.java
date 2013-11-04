package org.xbib.util;

import java.util.BitSet;

/**
 * A BitSet with fixed length
 */
public class FixedLengthBitSet extends BitSet implements Comparable<FixedLengthBitSet> {

    private int length;

    public FixedLengthBitSet(int length) {
        this.length = length;
    }

    public int fixedLength() {
        return length;
    }

    public FixedLengthBitSet subset(int start, int length) {
        assert (start + length <= this.length);
        FixedLengthBitSet result = new FixedLengthBitSet(length);
        for (int i = 0; i < length; i++) {
            result.set(i, this.get(start + i));
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FixedLengthBitSet)) {
            return false;
        }
        FixedLengthBitSet a = (FixedLengthBitSet) obj;
        return compareTo(a) == 0;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public int compareTo(FixedLengthBitSet bits) {
        return toString().compareTo(bits.toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(length);
        for (int i = length - 1; i >= 0; i--) {
            sb.append(get(i) ? '1' : '0');
        }
        return sb.toString();
    }
}
