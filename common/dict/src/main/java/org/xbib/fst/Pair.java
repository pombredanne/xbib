
package org.xbib.fst;

/**
 * Pairs two elements
 * <p/>
 * Original code obtained by
 * http://stackoverflow.com/questions/521171/a-java-collection-of-value-pairs-tuples
 */
public class Pair<L, R> {

    // The left element
    private L left;
    // The right element
    private R right;

    /**
     * Constructor specifying the left and right elements of the Pair.
     */
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Set the left element of the Pair
     */
    public void setLeft(L left) {
        this.left = left;
    }

    /**
     * Set the right element of the Pair
     */
    public void setRight(R right) {
        this.right = right;
    }

    /**
     * Get the left element of the Pair
     */
    public L getLeft() {
        return left;
    }

    /**
     * Get the right element of the Pair
     */
    public R getRight() {
        return right;
    }

    @Override
    public int hashCode() {
        return left.hashCode() ^ right.hashCode();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<L, R> pairo = (Pair<L, R>) o;
        return this.left.equals(pairo.getLeft())
                && this.right.equals(pairo.getRight());
    }

    @Override
    public String toString() {
        return "(" + left + ", " + right + ")";
    }
}
