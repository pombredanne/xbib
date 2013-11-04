
package org.xbib.fst.semiring;

/**
 * Probability semiring implementation.
 */
public class ProbabilitySemiring extends Semiring {

    @Override
    public float plus(float w1, float w2) {
        if (!isMember(w1) || !isMember(w2)) {
            return Float.NEGATIVE_INFINITY;
        }

        return w1 + w2;
    }

    @Override
    public float times(float w1, float w2) {
        if (!isMember(w1) || !isMember(w2)) {
            return Float.NEGATIVE_INFINITY;
        }

        return w1 * w2;
    }

    @Override
    public float divide(float w1, float w2) {
        return Float.NEGATIVE_INFINITY;
    }

    @Override
    public float zero() {
        return 0.f;
    }

    @Override
    public float one() {
        return 1.f;
    }

    @Override
    public boolean isMember(float w) {
        return !Float.isNaN(w) // not a NaN,
                && (w >= 0); // and positive
    }

    @Override
    public float reverse(float w1) {
        throw new UnsupportedOperationException();
    }

}
