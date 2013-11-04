package org.xbib.re;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.xbib.re.Expression.literal;
import static org.xbib.re.Expression.paren;


public abstract class Pattern {

    public enum CompileType {
        NFA,
        DFA,
    }

    private static final Sub START_SUB = new Sub(0, -1);

    private static final Sub[][] EMPTY = {};

    private final State start;

    private final int stateCount;

    private final int parenCount;

    private final State[] states;

    protected Pattern(String pattern, Expression expression) {
        if (expression.getStart().getOp() != State.Op.LParen) {
            throw new IllegalArgumentException("Outer expression must be paren");
        }
        this.start = expression.getStart();
        State s = State.MATCH;
        expression.patch(s);
        Set<State> st = new LinkedHashSet<State>();
        st.add(s);
        this.parenCount = visit(start, st);
        this.stateCount = st.size();
        this.states = st.toArray(new State[stateCount]);
    }

    public static Pattern compile(String regex) {
        return compile(regex, CompileType.DFA);
    }

    public static Pattern compile(String regex, CompileType type) {
        switch (type) {
            case NFA:
                return new NFA(regex, paren(literal(regex), 0));
            case DFA:
                return new DFA(regex, paren(literal(regex), 0));
        }
        return null;
    }

    public static boolean matches(String regex, CharSequence input) {
        return compile(regex).matcher(input).matches();
    }

    public static String quote(String s) {
        throw new UnsupportedOperationException("not implemented");
    }

    public Matcher matcher(CharSequence input) {
        return createMatcher().reset(input);
    }

    /*public String pattern() {
        return pattern;
    }

    @Override
    public String toString() {
        return pattern();
    }*/

    public String[] split(CharSequence input) {
        return split(input, 0);
    }

    public String[] split(CharSequence input, int limit) {
        List<String> parts = new ArrayList<>((limit <= 0) ? 10 : limit);
        Matcher m = matcher(input);
        int p = 0;
        for (int count = 0; ++count != limit && m.find(p); p = m.end()) {
            parts.add(input.subSequence(p, m.start()).toString());
        }
        parts.add(input.subSequence(p, input.length()).toString());
        if (limit == 0) {
            int size = parts.size();
            while ("".equals(parts.get(size - 1))) {
                parts.remove(--size);
            }
        }
        return parts.toArray(new String[parts.size()]);
    }

    protected abstract Matcher createMatcher();

    protected State startState() {
        return start;
    }

    protected int parenCount() {
        return parenCount;
    }

    protected int stateCount() {
        return stateCount;
    }

    protected void startSet(int p, Sub[][] threads) {
        step(EMPTY, 0, p, threads, new Sub[parenCount]);
    }

    protected void step(Sub[][] clist, int c, int p, Sub[][] nlist, Sub[] match) {
        Arrays.fill(nlist, null);
        for (int i = 0; i < clist.length; i++) {
            Sub[] tmatch = clist[i];
            if (tmatch == null) {
                continue;
            }
            State state = states[i];
            switch (state.getOp()) {
                case CharSet:
                    if (state.getCharSet().contains(c)) {
                        addState(nlist, state.getState1(), tmatch, p);
                    }
                    break;
                case NotCharSet:
                    if (!state.getCharSet().contains(c)) {
                        addState(nlist, state.getState1(), tmatch, p);
                    }
                    break;
                case Match:
                    System.arraycopy(tmatch, 0, match, 0, parenCount);
                    return;
            }
        }
        if (match[0] == null || match[0].sp < 0) {
            addState(nlist, start, new Sub[parenCount], p);
        }
    }

    private void addState(Sub[][] threads, State state, Sub[] match, int p) {
        if (state == null) {
            return;
        }
        int id = state.getId();
        if (threads[id] != null) {
            return;
        }
        threads[id] = new Sub[parenCount];
        System.arraycopy(match, 0, threads[id], 0, parenCount);
        switch (state.getOp()) {
            case Match:
            case CharSet:
            case NotCharSet:
                break;
            case Split:
                addState(threads, state.getState1(), match, p);
                addState(threads, state.getState2(), match, p);
                break;
            default:
                int data = state.getData();
                Sub save = match[data];
                match[data] = (state.getOp() == State.Op.LParen)
                        ? ((p == 0) ? START_SUB : new Sub(p, -1))
                        : new Sub(save.sp, p);
                match[data].setLabel(state.getLabel());
                addState(threads, state.getState1(), match, p);
                match[data] = save;
        }
    }

    private int visit(State state, Set<State> mark) {
        if (state == null || mark.contains(state)) {
            return 0;
        }
        state.setId(mark.size());
        mark.add(state);
        return ((state.getOp() == State.Op.LParen) ? 1 : 0)
                + visit(state.getState1(), mark)
                + visit(state.getState2(), mark);
    }

}
