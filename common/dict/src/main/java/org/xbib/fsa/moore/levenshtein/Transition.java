
package org.xbib.fsa.moore.levenshtein;

import org.xbib.util.FixedLengthBitSet;

class Transition {

    private PositionState from;

    private FixedLengthBitSet b;

    private PositionState to;

    public Transition(PositionState from, FixedLengthBitSet b, PositionState to) {
        this.from = from;
        this.b = b;
        this.to = to;
    }

    public PositionState getFrom() {
        return from;
    }

    public FixedLengthBitSet getString() {
        return b;
    }

    public PositionState getTo() {
        return to;
    }

}
