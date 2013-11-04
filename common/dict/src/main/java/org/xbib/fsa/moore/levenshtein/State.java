package org.xbib.fsa.moore.levenshtein;

import java.util.Collection;

/**
 * A State for an Automaton.
 *
 * @param <T>
 * @param <E>
 */
public interface State<T, E> {

    /**
     * Get all symbols that describe transitions from this state
     * to other states.
     *
     * @return a Collection of terminal symbols T
     */
    Collection<T> getTransitionSymbols();

    /**
     * Get all states following this state.
     *
     * @return a Collection of States
     */
    Collection<State<T, E>> getFollowers();

    /**
     * Get the next state determined by a symbol
     *
     * @param t the terminal symbol
     * @return a State or null if a State does not exist
     */
    State<T, E> getNextState(T t);

    /**
     * Add a new state following this state.
     *
     * @param t
     * @param next
     * @return the State added
     */
    State<T, E> addNextState(T t, State<T, E> next);

    /**
     * Get the number of following states.
     *
     * @return
     */
    int getNextStateCount();

    /**
     * Check if this state accepts.
     *
     * @return true if this state accepts
     */
    boolean isAccept();

    /**
     * Attach an acceptance value to this state.
     *
     * @param element
     */
    void setElement(E element);

    /**
     * Get the element from this state
     *
     * @return element
     */
    E getElement();

}
