
package org.xbib.fst;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The fst's mutable state implementation.
 * <p/>
 * Holds its outgoing {@link Arc} objects in a List
 * allowing additions/deletions
 */
public class State {

    // State's Id
    private int id = -1;
    // Final weight
    private float fnlWeight;
    // Outgoing arcs
    private List<Arc> arcs = null;
    // initial number of arcs
    protected int initialNumArcs = -1;

    /**
     * Default Constructor
     */
    protected State() {
        arcs = new ArrayList<Arc>();
    }

    /**
     * Constructor specifying the state's final weight
     *
     * @param fnlWeight
     */
    public State(float fnlWeight) {
        this();
        this.fnlWeight = fnlWeight;
    }

    /**
     * Constructor specifying the initial capacity of the arc's ArrayList (this
     * is an optimization used in various operations)
     *
     * @param initialNumArcs
     */
    public State(int initialNumArcs) {
        this.initialNumArcs = initialNumArcs;
        if (initialNumArcs > 0) {
            arcs = new ArrayList<Arc>(initialNumArcs);
        }
    }

    /**
     * Sorts the arc's ArrayList based on the provided Comparator
     */
    public void arcSort(Comparator<Arc> cmp) {
        Collections.sort(arcs, cmp);
    }

    /**
     * Get the state's final Weight
     */
    public float getFinalWeight() {
        return fnlWeight;
    }

    /**
     * Set the state's arcs list
     *
     * @param arcs the arcs list to set
     */
    public void setArcs(List<Arc> arcs) {
        this.arcs = arcs;
    }

    /**
     * Set the state's final weight
     *
     * @param fnlfloat the final weight to set
     */
    public void setFinalWeight(float fnlfloat) {
        this.fnlWeight = fnlfloat;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get the state's id
     */
    public int getId() {
        return id;
    }

    /**
     * Get the number of outgoing arcs
     */
    public int getNumArcs() {
        return arcs.size();
    }

    /**
     * Add an outgoing arc to the state
     *
     * @param arc the arc to add
     */
    public void addArc(Arc arc) {
        arcs.add(arc);
    }

    /**
     * Set an arc at the specified position in the arcs' ArrayList.
     *
     * @param index the position to the arcs' array
     * @param arc   the arc value to set
     */
    public void setArc(int index, Arc arc) {
        arcs.set(index, arc);
    }

    /**
     * Get an arc based on it's index the arcs ArrayList
     *
     * @param index the arc's index
     * @return the arc
     */
    public Arc getArc(int index) {
        return arcs.get(index);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        State other = (State) obj;
        if (id != other.id) {
            return false;
        }
        if (!(fnlWeight == other.fnlWeight)) {
            if (Float.floatToIntBits(fnlWeight) != Float
                    .floatToIntBits(other.fnlWeight)) {
                return false;
            }
        }
        if (arcs == null) {
            if (other.arcs != null) {
                return false;
            }
        } else if (!arcs.equals(other.arcs)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(id).append(", ").append(fnlWeight).append(")");
        return sb.toString();
    }

    /**
     * Delete an arc based on its index
     *
     * @param index the arc's index
     * @return the deleted arc
     */
    public Arc deleteArc(int index) {
        return arcs.remove(index);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

}
