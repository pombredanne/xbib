package org.xbib.re;

public final class Edge {

    private State state;

    public Edge() {
    }

    public Edge(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        if (this.state != null && this.state != state) {
            throw new IllegalStateException("Subexpressions cannot be shared across patterns");
        }
        this.state = state;
    }
}
