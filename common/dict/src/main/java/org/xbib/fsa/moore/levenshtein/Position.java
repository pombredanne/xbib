
package org.xbib.fsa.moore.levenshtein;

class Position implements Comparable<Position> {

    private Parameter parameter;

    private Type type;

    private int index;

    private int error;

    public Position(Parameter parameter, Type type, int index, int error) {
        this.parameter = parameter;
        this.type = type;
        this.index = index;
        this.error = error;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public Type getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

    public int getError() {
        return error;
    }

    @Override
    public int compareTo(Position p) {
        if ((p.getParameter() == parameter)
                && (p.getType() == type)
                && (p.getIndex() == index)
                && (p.getError() == error)) {
            return 0;
        }
        if (parameter != p.getParameter()) {
            return (parameter == Parameter.I ? -1 : 1);
        }
        if (type != p.getType()) {
            return (type == Type.USUAL ? -1 : 1);
        }
        if (index < p.getIndex()) {
            return -1;
        } else if (index > p.getIndex()) {
            return 1;
        }
        if (error < p.getError()) {
            return -1;
        } else if (error > p.getError()) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Position) {
            return compareTo((Position) o) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getParameter().hashCode()
                ^ getType().hashCode()
                ^ (getIndex() * 0x00010000)
                ^ getError();
    }
}
