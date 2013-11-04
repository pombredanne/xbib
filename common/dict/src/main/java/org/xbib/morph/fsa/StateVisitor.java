package org.xbib.morph.fsa;

/**
 * State visitor.
 *
 * @see FSA#visitInPostOrder(org.xbib.morph.fsa.StateVisitor)
 * @see FSA#visitInPreOrder(org.xbib.morph.fsa.StateVisitor)
 */
public interface StateVisitor {

    public boolean accept(int state);
}