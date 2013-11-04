package org.xbib.re;

public final class State {

    public enum Op {

        NotCharSet,
        CharSet,
        Split,
        LParen,
        RParen,
        Match
    }

    ;
    public static final State MATCH = new State(Op.Match, 0, null, null, null);

    static {
        MATCH.setId(0);
    }

    private final Op op;

    private final int data;

    private final CharSet cset;

    private final Edge edge1;

    private final Edge edge2;

    private int id = -1;

    private Object label;

    public static State charSet(CharSet cset, Edge edge, boolean negate) {
        return new State(negate ? Op.NotCharSet : Op.CharSet, 0, cset, edge, null);
    }

    public static State lParen(int data, Edge edge) {
        return new State(Op.LParen, data, null, edge, null);
    }

    public static State rParen(int data, Edge edge) {
        return new State(Op.RParen, data, null, edge, null);
    }

    public static State split(Edge edge1, Edge edge2) {
        return new State(Op.Split, 0, null, edge1, edge2);
    }

    private State(Op op, int data, CharSet cset, Edge edge1, Edge edge2) {
        this.op = op;
        this.data = data;
        this.cset = cset;
        this.edge1 = edge1;
        this.edge2 = edge2;
    }

    public Op getOp() {
        return op;
    }

    public int getData() {
        return data;
    }

    public CharSet getCharSet() {
        return cset;
    }

    public State getState1() {
        return (edge1 != null) ? edge1.getState() : null;
    }

    public State getState2() {
        return (edge2 != null) ? edge2.getState() : null;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    public void setId(int id) {
        if (this.id != -1 && this.id != id) {
            throw new IllegalStateException("Subexpressions cannot be shared across patterns");
        }
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setLabel(Object label) {
        this.label = label;
    }

    public Object getLabel() {
        return label;
    }
}
