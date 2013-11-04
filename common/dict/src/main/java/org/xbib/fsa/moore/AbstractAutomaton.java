package org.xbib.fsa.moore;

import org.xbib.fsa.moore.levenshtein.State;
import org.xbib.fsa.moore.Automaton;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Abstract automaton class
 */
public abstract class AbstractAutomaton<T, E> implements Automaton<T, E> {

    private String name;

    private State<T, E> root = newState();

    /**
     * Initialize an empty AbstractAutomaton.
     */
    public AbstractAutomaton() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Add element to automaton, reachable by input.
     *
     * @param input
     * @param element
     */
    @Override
    public void add(T[] input, E element) {
        State<T, E> current = root;
        for (T symbol : input) {
            State<T, E> next = current.getNextState(symbol);
            if (next == null) {
                next = current.addNextState(symbol, newState());
            }
            current = next;
        }
        current.setElement(element);
    }

    protected abstract State<T, E> newState();

    /**
     * Get the current state of the automaton.
     *
     * @return the current state.
     */
    @Override
    public State<T, E> getCurrentState() {
        return root;
    }

    public Set<State<T, E>> getStates() {
        Set<State<T, E>> visited = new HashSet();
        LinkedList<State<T, E>> worklist = new LinkedList();
        worklist.add(root);
        visited.add(root);
        while (worklist.size() > 0) {
            State<T, E> s = worklist.removeFirst();
            Collection<State<T, E>> tr = s.getFollowers();
            for (State st : tr) {
                if (!visited.contains(st)) {
                    visited.add(st);
                    worklist.add(st);
                }
            }
        }
        return visited;
    }

    /**
     * Returns <a href="http://www.research.att.com/sw/tools/graphviz/" target="_top">Graphviz Dot</a>
     * representation of this automaton.
     */
    public String toDot() {
        StringBuilder b = new StringBuilder("digraph Automaton {\n");
        b.append("  rankdir = LR;\n");
        for (State<T, E> s : getStates()) {
            b.append("  ").append(s.getElement());
            if (s.isAccept()) {
                b.append(" [shape=doublecircle,label=\"\"];\n");
            } else {
                b.append(" [shape=circle,label=\"\"];\n");
            }
            if (s == root) {
                b.append("  initial [shape=plaintext,label=\"\"];\n");
                b.append("  initial -> ").append(s.getElement()).append("\n");
            }
            Collection<T> ts = s.getTransitionSymbols();
            for (T t : ts) {
                b.append("  ").append(s.getElement());
                State st = s.getNextState(t);
                b.append(" -> ").append(st.getElement()).append(" [label=\"").append(t).append("\"]\n");
            }
        }
        return b.append("}\n").toString();
    }

}
