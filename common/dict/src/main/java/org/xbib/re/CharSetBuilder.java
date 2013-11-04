package org.xbib.re;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CharSetBuilder {

    private static final Comparator<Range> START_COMPARATOR = new Comparator<Range>() {

        @Override
        public int compare(Range r1, Range r2) {
            return r1.start - r2.start;
        }
    };

    private final List<Range> ranges = Generics.newArrayList();

    private final Range key = new Range();

    private static class Range {

        int start;
        int end;
    }

    public CharSetBuilder add(CharSequence chars) {
        for (int i = 0, len = chars.length(); i < len; i++) {
            add(chars.charAt(i));
        }
        return this;
    }

    public CharSetBuilder add(CharSet cset) {
        for (int c = cset.nextChar(0); c >= 0; c = cset.nextChar(c + 1)) {
            add(c);
        }
        return this;
    }

    public CharSetBuilder add(int start, int end) {
        assert start <= end;
        for (int c = start; c <= end; c++) {
            add(c);
        }
        return this;
    }

    public CharSetBuilder add(int c) {
        key.start = c;
        int index = Collections.binarySearch(ranges, key, START_COMPARATOR);
        if (index >= 0) {
            return this;
        }
        index = -(index + 1);
        if (index > 0) {
            Range r = ranges.get(index - 1);
            if (r.end >= c) {
                return this;
            }
            if (r.end + 1 == c) {
                r.end++;
                return this;
            }
        }
        if (index < ranges.size()) {
            Range r = ranges.get(index);
            if (r.start <= c) {
                return this;
            }
            if (r.start - 1 == c) {
                r.start--;
                return this;
            }
        }
        Range r = new Range();
        r.start = r.end = c;
        ranges.add(index, r);
        return this;
    }

    public String toString() {
        return ranges.toString();
    }

    public boolean isEmpty() {
        return ranges.isEmpty();
    }

    public CharSet build() {
        try {
            switch (ranges.size()) {
                case 0:
                    return EmptyCharSet.INSTANCE;
                case 1:
                    Range r = ranges.get(0);
                    if (r.start == r.end) {
                        return new SingleCharSet(r.start);
                    }
            }
            return new RangeCharSet(new ArrayList(ranges));
        } finally {
            ranges.clear();
        }
    }

    final private static class RangeCharSet extends CharSet {

        private final List<Range> ranges;
        private final Range key = new Range();
        private final int cardinality;

        public RangeCharSet(List<Range> ranges) {
            this.ranges = ranges;
            int total = 0;
            for (Range r : ranges) {
                total += r.end - r.start + 1;
            }
            cardinality = total;
            assert cardinality > 0;
        }

        public int cardinality() {
            return cardinality;
        }

        public boolean isEmpty() {
            return false;
        }

        public boolean contains(int c) {
            key.start = c;
            int index = Collections.binarySearch(ranges, key, START_COMPARATOR);
            if (index >= 0) {
                return true;
            }
            if (index == -1) {
                return false;
            }
            Range r = ranges.get(-index - 2);
            return c >= r.start && c <= r.end;
        }

        public int nextChar(int c) {
            key.start = c;
            int index = Collections.binarySearch(ranges, key, START_COMPARATOR);
            if (index >= 0) {
                return c;
            }
            index = -(index + 1);
            if (index > 0 && ranges.get(index - 1).end >= c) {
                return c;
            }
            if (index == ranges.size()) {
                return -1;
            }
            return ranges.get(index).start;
        }

        // TODO: provide more efficient impl if cset is also range
        public CharSet intersect(CharSet cset) {
            if (cset.isEmpty()) {
                return cset;
            }
            if (cset.cardinality() < cardinality) {
                return combine(cset, this, false);
            }
            return combine(this, cset, false);
        }

        // TODO: provide more efficient impl if cset is also range
        public CharSet subtract(CharSet cset) {
            if (cset.isEmpty()) {
                return this;
            }
            return combine(this, cset, true);
        }
    }

    private static CharSet combine(CharSet cset1, CharSet cset2, boolean subtract) {
        // TODO: knowing cardinality would help here (want to iterate over smaller one)
        CharSetBuilder builder = new CharSetBuilder();
        for (int c = cset1.nextChar(0); c >= 0; c = cset1.nextChar(c + 1)) {
            if (cset2.contains(c) ^ subtract) {
                builder.add(c);
            }
        }
        return builder.build();
    }
}
