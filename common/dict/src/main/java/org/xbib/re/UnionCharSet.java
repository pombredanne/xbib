package org.xbib.re;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class UnionCharSet extends GenericCharSet {

    private static final int BUCKET_CUTOFF = 1000;

    private static final Function<CharSet, Boolean> F_EMPTY = new Function<CharSet, Boolean>() {

        @Override
        public Boolean eval(CharSet cset) {
            return !cset.isEmpty();
        }
    };

    private static final Function<CharSet, Boolean> F_BUCKET = new Function<CharSet, Boolean>() {

        @Override
        public Boolean eval(CharSet cset) {
            return cset.cardinality() < BUCKET_CUTOFF;
        }
    };

    private final CharSet[] csets;

    public static CharSet union(List<CharSet> csets) {
        List<CharSet> copy = Generics.newArrayList(csets);
        Generics.filter(copy, F_EMPTY);
        if (copy.isEmpty()) {
            return EmptyCharSet.INSTANCE;
        }

        Map<Boolean, List<CharSet>> buckets = Generics.bucket(copy, F_BUCKET);
        copy.clear();
        copy.addAll(buckets.get(false));

        CharSetBuilder builder = new CharSetBuilder();
        for (CharSet cset : buckets.get(true)) {
            builder.add(cset);
        }
        if (!builder.isEmpty()) {
            copy.add(builder.build());
        }

        Collections.sort(csets, CardinalityComparator.INSTANCE);
        Collections.sort(copy, CardinalityComparator.INSTANCE);

        long total = 0;
        for (CharSet cset : copy) {
            total += cset.cardinality();
        }
        int cardinality = (int) Math.min(total, UNKNOWN_CARDINALITY);

        return new UnionCharSet(copy.toArray(new CharSet[copy.size()]), cardinality);
    }

    private UnionCharSet(CharSet[] csets, int cardinality) {
        super(cardinality);
        assert csets.length > 0;
        this.csets = csets;
    }

    @Override
    public boolean contains(int c) {
        for (CharSet cset : csets) {
            if (cset.contains(c)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int nextChar(int c) {
        int min = Integer.MAX_VALUE;
        for (CharSet cset : csets) {
            int n = cset.nextChar(c);
            if (n >= 0) {
                min = Math.min(min, n);
            }
        }
        return (min == Integer.MAX_VALUE) ? -1 : min;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
