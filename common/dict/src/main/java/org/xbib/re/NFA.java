package org.xbib.re;

import java.util.Arrays;

public class NFA extends Pattern {

    public NFA(Expression e) {
        this(null, e);
    }

    public NFA(String regex, Expression e) {
        super(regex, e);
    }

    @Override
    protected Matcher createMatcher() {
        return new NFAMatcher(this);
    }

    private static class NFAMatcher extends Matcher {

        private Sub[][] empty;
        private Sub[][] clist;
        private Sub[][] nlist;
        private Sub[][] t;

        public NFAMatcher(NFA pattern) {
            super(pattern);
            empty = new Sub[pattern.stateCount()][];
            clist = new Sub[pattern.stateCount()][];
            nlist = new Sub[pattern.stateCount()][];
        }

        @Override
        protected void match(int p) {
            pattern.startSet(p, clist);
            for (int len = input.length(); p < len; p++) {
                pattern.step(clist, input.charAt(p), p + 1, nlist, match);
                t = clist;
                clist = nlist;
                nlist = t; // swap
                if (Arrays.equals(clist, empty)) { // TODO: need different data structure to make this efficient
                    break;
                }
            }
            pattern.step(clist, 0, p, nlist, match);
        }
    }
}
