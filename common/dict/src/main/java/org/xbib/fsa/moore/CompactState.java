
package org.xbib.fsa.moore;

import org.xbib.fsa.moore.levenshtein.State;
import org.xbib.util.CompactHashMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * State for FSA using CompactHashMap for state transitions.
 *
 * @param <T>
 * @param <E>
 */
public class CompactState<T, E> implements State<T, E> {

    private E element;

    private Map<T, State<T, E>> followers;

    public CompactState() {
        this(null);
    }

    public CompactState(E element) {
        this.element = element;
        this.followers = new CompactHashMap<T, State<T, E>>();
    }

    @Override
    public Set<T> getTransitionSymbols() {
        return followers.keySet();
    }

    @Override
    public Collection<State<T, E>> getFollowers() {
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
    public State<T, E> addNextState(T t, State<T, E> next) {
        if (followers.containsKey(t)) {
            return followers.get(t);
        }
        followers.put(t, next);
        return next;
    }

    @Override
    public State<T, E> getNextState(T t) {
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
