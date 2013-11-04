package org.xbib.re;

import java.util.Comparator;

/**
 * sorts by descending cardinality (largest first)
 */
class CardinalityComparator implements Comparator<CharSet> {
    public static final Comparator<CharSet> INSTANCE = new CardinalityComparator();

    @Override
    public int compare(CharSet cset1, CharSet cset2) {
        int card1 = cset1.cardinality();
        int card2 = cset2.cardinality();
        return (card1 < card2) ? 1 : (card2 < card1) ? -1 : 0;
    }
}
