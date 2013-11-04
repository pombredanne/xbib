package org.xbib.fsa.moore.levenshtein;

import java.util.Collection;
import java.util.TreeSet;

class PositionState extends TreeSet<Position>
        implements Comparable<PositionState> {

    public PositionState() {
        super();
    }

    public PositionState(Collection<? extends Position> c) {
        super(c);
    }

    public boolean contains(Position p) {
        for (Position t : this) {
            if (t.equals(p)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PositionState) {
            return equals((PositionState) o);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (Position p : this) {
            hash ^= p.hashCode();
        }
        return hash;
    }

    public boolean equals(PositionState s) {
        if (s.size() != size()) {
            return false;
        }
        for (Position p : s) {
            if (!this.contains(p)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareTo(PositionState s) {
        if (size() < s.size()) {
            return -1;
        } else if (size() > s.size()) {
            return 1;
        }
        if (equals(s)) {
            return 0;
        }
        return -1;
    }
}
