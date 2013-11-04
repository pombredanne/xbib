package org.xbib.re;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

public final class Expression {

    private final State start;
    private final List<Edge> out;

    public Pattern compile(String regex, Pattern.CompileType type) {
        switch (type) {
            case NFA:
                return new NFA(regex, this);
            case DFA:
                return new DFA(regex, this);
        }
        return null;
    }

    private Expression(State start, List<Edge> out) {
        assert !out.isEmpty();
        this.start = start;
        this.out = out;
    }

    protected State getStart() {
        return start;
    }

    protected void patch(State state) {
        for (Edge edge : out) {
            edge.setState(state);
        }
    }

    private static List<Edge> append(final List<Edge> left, final List<Edge> right) {
        return new AbstractList<Edge>() {

            @Override
            public Edge get(int index) {
                int leftSize = left.size();
                return (index < leftSize) ? left.get(index) : right.get(index - leftSize);
            }

            @Override
            public int size() {
                return left.size() + right.size();
            }
        };
    }

    public static Expression literal(String value) {
        Expression frag = literal(new SingleCharSet(value.charAt(0)));
        for (int i = 1, len = value.length(); i < len; i++) {
            frag = concat(frag, literal(new SingleCharSet(value.charAt(i))));
        }
        return frag;
    }

    public static Expression literal(CharSet cset) {
        return literal(cset, false);
    }

    public static Expression literal(CharSet cset, boolean negate) {
        Edge edge = new Edge();
        return new Expression(State.charSet(cset, edge, negate), Collections.singletonList(edge));
    }

    public static Expression concat(Expression left, Expression right) {
        left.patch(right.start);
        return new Expression(left.start, right.out);
    }

    public static Expression concat(Expression... frags) {
        return concat(0, frags);
    }

    private static Expression concat(int index, Expression[] frags) {
        return concat(frags[index], (index + 2 == frags.length) ? frags[index + 1] : concat(index + 1, frags));
    }

    public static Expression or(Expression left, Expression right) {
        return new Expression(State.split(new Edge(left.start), new Edge(right.start)),
                append(left.out, right.out));
    }

    public static Expression or(Expression... frags) {
        return or(0, frags);
    }

    private static Expression or(int index, Expression[] frags) {
        return or(frags[index], (index + 2 == frags.length) ? frags[index + 1] : or(index + 1, frags));
    }

    public static Expression repeat(Expression e, int min, int max) {
        if (min < 0 || max < 0 || (max < min && max != 0)) {
            throw new IllegalArgumentException("min=" + min + " max=" + max);
        }

        if (((max | min) & 1) <= 1) {
            if (min == 1 && max == 1) {
                return e;
            }
            Edge edge = new Edge();
            State s = State.split(new Edge(e.start), edge);
            List<Edge> ptr = Collections.singletonList(edge);
            if (max == 0) {
                e.patch(s);
                return (min == 0) ? new Expression(s, ptr) : new Expression(e.start, ptr);
            } else { // min == 0, max == 1
                return new Expression(s, append(e.out, ptr));
            }
        } else if (max == 0) { // min > 1, max == 0
            return concat(repeat(e, min - 1, min - 1), repeat(e, 1, 0));
        } else if (max == min) { // min > 1, max == min
            Expression tmp = e;
            for (int i = 1; i < min; i++) {
                tmp = concat(tmp, e);
            }
            return tmp;
        } else if (min > 0) { // min > 0, max > min
            return concat(repeat(e, min, min), repeat(e, 0, max - min));
        } else { // min == 0, max > 0
            return repeat(repeat(e, 0, 1), max, max);
        }
    }

    public static Expression paren(Expression e, int n) {
        return paren(e, n, null);
    }

    public static Expression paren(Expression e, int n, String label) {
        Edge edge = new Edge();
        State s = State.rParen(n, edge);
        s.setLabel(label);
        e.patch(s);
        return new Expression(State.lParen(n, new Edge(e.start)), Collections.singletonList(edge));
    }
}
