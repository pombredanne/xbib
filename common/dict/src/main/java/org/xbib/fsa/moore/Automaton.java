package org.xbib.fsa.moore;

import org.xbib.fsa.moore.levenshtein.State;

import java.util.Set;

/**
 * Automaton interface.
 * <p/>
 * An automaton is constructed by State objects, describing how
 * a given input is recognized as valid input.
 * <p/>
 * The input for construction are terminal symbols T[], attached by
 * an element E, representing the acceptance.
 * <p/>
 * The alphabet of the automaton is a set of symbols, Set<T>.
 * <p/>
 * Automata are used by looking at the current state and traversing
 * through following states, examining if an input is accepted.
 * <p/>
 * If multiple states are active, the automaton is non-deterministic,
 * if only one state is active, the automaton is deterministic.
 *
 * @param <T> a terminal symbol
 * @param <E> an element attached to an input
 */
public interface Automaton<T, E> {

    void add(T[] input, E element);

    Set<T> getAlphabet();

    State<T, E> getCurrentState();

}
