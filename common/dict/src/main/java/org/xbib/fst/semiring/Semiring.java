
package org.xbib.fst.semiring;

/**
 * Abstract semiring
 */
public abstract class Semiring {

    /**
     * Semiring's plus operation
     */
    public abstract float plus(float w1, float w2);

    public abstract float reverse(float w1);

    /**
     * Semiring's times operation
     */
    public abstract float times(float w1, float w2);

    /**
     * Semiring's divide operation
     */
    public abstract float divide(float w1, float w2);

    /**
     * Semiring's zero element
     */
    public abstract float zero();

    /**
     * Semiring's one element
     */
    public abstract float one();

    /**
     * Checks if a value is a valid one the semiring
     */
    public abstract boolean isMember(float w);

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass();
    }

    @Override
    public String toString() {
        return this.getClass().toString();
    }

    /**
     * NATURAL ORDER
     * <p/>
     * By definition: a <= b iff a + b = a
     * <p/>
     * The natural order is a negative partial order iff the semiring is
     * idempotent. It is trivially monotonic for plus. It is left (resp. right)
     * monotonic for times iff the semiring is left (resp. right) distributive.
     * It is a total order iff the semiring has the path property.
     * <p/>
     * See Mohri,
     * "Semiring Framework and Algorithms for Shortest-Distance Problems",
     * Journal of Automata, Languages and Combinatorics 7(3):321-350, 2002.
     * <p/>
     * We define the strict version of this order below.
     */
    public boolean naturalLess(float w1, float w2) {
        return (this.plus(w1, w2) == w1) && (w1 != w2);
    }

}