package org.xbib.fsa.moore;

import org.xbib.fsa.moore.levenshtein.State;
import org.xbib.util.CompactHashMap;

import java.util.Collection;

/**
 * @param <T>
 * @param <E>
 */
class CodePointState<T extends Character, E> implements State<Character, E> {

    private E element;
    private final CompactHashMap<Character, State<Character, E>> followers;

    protected CodePointState() {
        this(null);
    }

    protected CodePointState(E element) {
        this.element = element;
        this.followers = new CompactHashMap<Character, State<Character, E>>();
    }

    @Override
    public Collection<Character> getTransitionSymbols() {
        return followers.keySet();
    }

    @Override
    public Collection<State<Character, E>> getFollowers() {
        return followers.values();
    }

    @Override
    public int getNextStateCount() {
        return followers.size();
    }

    /**
     * Add a next state with the specified transition.
     *
     * @param t    Transition to the next state.
     * @param next the next state
     * @return the new state associated with the specified transition.
     */
    @Override
    public State<Character, E> addNextState(Character t, State<Character, E> next) {
        if (followers.containsKey(t)) {
            return followers.get(t);
        }
        followers.put(t, next);
        return next;
    }

    @Override
    public State<Character, E> getNextState(Character t) {
        return followers.get(t);
    }

    /**
     * Determine if the state is an output accepting state.
     *
     * @return True if the state is an accepting state,
     *         false otherwise.
     */
    @Override
    public boolean isAccept() {
        return element != null;
    }

    /**
     * Get the element stored in this state.
     *
     * @return Element stored in this state.
     */
    @Override
    public E getElement() {
        return element;
    }

    @Override
    public void setElement(E element) {
        this.element = element;
    }
}
