package org.xbib.re;

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

public class DFA extends Pattern {

    private final EquivMap equiv;

    public DFA(Expression e) {
        this(null, e);
    }

    public DFA(String regex, Expression e) {
        super(regex, e);
        this.equiv = new EquivMap(startState());
    }

    @Override
    protected Matcher createMatcher() {
        //return new DFAMatcher(this, equiv, dstates);
        return new DFAMatcher(this, equiv, new WeakHashMap<Object, DState>());
    }

    private class DFAMatcher extends Matcher {

        private Sub[][] nlist;
        private final EquivMap equiv;
        private final Map<Object, DState> dstates;

        public DFAMatcher(DFA pattern, EquivMap equiv, Map<Object, DState> dstates) {
            super(pattern);
            nlist = new Sub[pattern.stateCount()][];
            this.equiv = equiv;
            this.dstates = dstates;
        }

        @Override
        protected void match(int p) {
            dstates.clear(); // TODO: problem!!!!
            pattern.startSet(p, nlist);
            DState d = dstate(nlist);
            DState next;
            for (int len = input.length(); p < len; p++) { // TODO: short circuit
                char c = input.charAt(p);
                int index = equiv.getIndex(c);
                if ((next = d.next[index]) == null) {
                    pattern.step(d.threads, c, p + 1, nlist, match);
                    next = d.next[index] = dstate(nlist);
                }
                d = next;
            }
            pattern.step(d.threads, 0, p, nlist, match);
        }

        private DState dstate(Sub[][] threads) {
            Object key = new DeepKey(threads);
            DState d = dstates.get(key);
            if (d == null) {
                Sub[][] copy = new Sub[threads.length][];
                System.arraycopy(threads, 0, copy, 0, threads.length); // TODO: deep clone necessary?
                dstates.put(new DeepKey(copy, key.hashCode()),
                        d = new DState(copy, equiv.size()));
            }
            return d;
        }
    }

    private static class DState {

        final Sub[][] threads;
        final DState[] next;

        public DState(Sub[][] threads, int size) {
            this.threads = threads;
            this.next = new DState[size];
        }
    }

    private static class DeepKey {

        private final Object[] a;
        private final int hashCode;

        public DeepKey(Object[] a) {
            this(a, Arrays.deepHashCode(a));
        }

        public DeepKey(Object[] a, int hashCode) {
            this.a = a;
            this.hashCode = hashCode;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DeepKey)) {
                return false;
            }
            return Arrays.deepEquals(a, ((DeepKey) o).a);
        }

        @Override
        public String toString() {
            return Arrays.deepToString(a);
        }
    }
}
