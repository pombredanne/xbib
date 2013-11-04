
package org.xbib.fst.operations;

import org.xbib.fst.Arc;
import org.xbib.fst.Fst;
import org.xbib.fst.State;
import org.xbib.fst.semiring.Semiring;

import java.util.HashMap;
import java.util.Map;

/**
 * Remove epsilon operation.
 */
public class RmEpsilon {
    /**
     * Default Constructor
     */
    private RmEpsilon() {
    }

    /**
     * Put a new state in the epsilon closure
     */
    private static void put(State fromState, State toState, float weight,
                            Map<State, Float>[] cl) {
        Map<State, Float> tmp = cl[fromState.getId()];
        if (tmp == null) {
            tmp = new HashMap<State, Float>();
            cl[fromState.getId()] = tmp;
        }
        tmp.put(toState, weight);
    }

    /**
     * Add a state in the epsilon closure
     */
    private static void add(State fromState, State toState, float weight,
                            Map<State, Float>[] cl, Semiring semiring) {
        Float old = getPathWeight(fromState, toState, cl);
        if (old == null) {
            put(fromState, toState, weight, cl);
        } else {
            put(fromState, toState, semiring.plus(weight, old), cl);
        }

    }

    /**
     * Calculate the epsilon closure
     */
    private static void calcClosure(Fst fst, State state,
                                    Map<State, Float>[] cl, Semiring semiring) {
        float pathWeight;
        int numArcs = state.getNumArcs();
        for (int j = 0; j < numArcs; j++) {
            Arc a = state.getArc(j);
            if ((a.getInputLabel() == 0) && (a.getOutputLabel() == 0)) {
                if (cl[a.getNextState().getId()] == null) {
                    calcClosure(fst, a.getNextState(), cl, semiring);
                }
                if (cl[a.getNextState().getId()] != null) {
                    for (State pathFinalState : cl[a.getNextState().getId()]
                            .keySet()) {
                        pathWeight = semiring.times(
                                getPathWeight(a.getNextState(), pathFinalState,
                                        cl), a.getWeight());
                        add(state, pathFinalState, pathWeight, cl, semiring);
                    }
                }
                add(state, a.getNextState(), a.getWeight(), cl, semiring);
            }
        }
    }

    /**
     * Get an epsilon path's cost in epsilon closure
     */
    private static Float getPathWeight(State in, State out,
                                       Map<State, Float>[] cl) {
        if (cl[in.getId()] != null) {
            return cl[in.getId()].get(out);
        }

        return null;
    }

    /**
     * Removes epsilon transitions from an fst.
     * <p/>
     * It return a new epsilon-free fst and does not modify the original fst
     *
     * @param fst the fst to remove epsilon transitions from
     * @return the epsilon-free fst
     */
    public static Fst get(Fst fst) {
        if (fst == null) {
            return null;
        }

        if (fst.getSemiring() == null) {
            return null;
        }

        Semiring semiring = fst.getSemiring();

        Fst res = new Fst(semiring);

        Map<State, Float>[] cl = new HashMap[fst.getNumStates()];
        for (int i = 0; i < cl.length; i++) {
            cl[i] = null;
        }
        State[] oldToNewStateMap = new State[fst.getNumStates()];
        State[] newToOldStateMap = new State[fst.getNumStates()];
        for (int i = 0; i < fst.getNumStates(); i++) {
            oldToNewStateMap[i] = null;
            newToOldStateMap[i] = null;
        }

        int numStates = fst.getNumStates();
        for (int i = 0; i < numStates; i++) {
            State s = fst.getState(i);
            // Add non-epsilon arcs
            State newState = new State(s.getFinalWeight());
            res.addState(newState);
            oldToNewStateMap[s.getId()] = newState;
            newToOldStateMap[newState.getId()] = s;
            if (newState.getId() == fst.getStart().getId()) {
                res.setStart(newState);
            }
        }

        for (int i = 0; i < numStates; i++) {
            State s = fst.getState(i);
            // Add non-epsilon arcs
            State newState = oldToNewStateMap[s.getId()];
            int numArcs = s.getNumArcs();
            for (int j = 0; j < numArcs; j++) {
                Arc a = s.getArc(j);
                if ((a.getInputLabel() != 0) || (a.getOutputLabel() != 0)) {
                    newState.addArc(new Arc(a.getInputLabel(), a.getOutputLabel(), a
                            .getWeight(), oldToNewStateMap[a.getNextState()
                            .getId()]));
                }
            }

            // Compute e-Closure
            if (cl[s.getId()] == null) {
                calcClosure(fst, s, cl, semiring);
            }
        }

        // augment fst with arcs generated from epsilon moves.
        numStates = res.getNumStates();
        for (int i = 0; i < numStates; i++) {
            State s = res.getState(i);
            State oldState = newToOldStateMap[s.getId()];
            if (cl[oldState.getId()] != null) {
                for (State pathFinalState : cl[oldState.getId()].keySet()) {
                    if (pathFinalState.getFinalWeight() != semiring.zero()) {
                        s.setFinalWeight(semiring.plus(s.getFinalWeight(),
                                semiring.times(getPathWeight(oldState, pathFinalState, cl),
                                        pathFinalState.getFinalWeight())));
                    }
                    int numArcs = pathFinalState.getNumArcs();
                    for (int j = 0; j < numArcs; j++) {
                        Arc a = pathFinalState.getArc(j);
                        if ((a.getInputLabel() != 0) || (a.getOutputLabel() != 0)) {
                            Arc newArc = new Arc(a.getInputLabel(), a.getOutputLabel(),
                                    semiring.times(a.getWeight(),
                                            getPathWeight(oldState, pathFinalState, cl)),
                                    oldToNewStateMap[a.getNextState().getId()]);
                            s.addArc(newArc);
                        }
                    }
                }
            }
        }

        res.setInputSymbols(fst.getInputSymbols());
        res.setOutputSymbols(fst.getOutputSymbols());

        Connect.apply(res);

        return res;
    }
}
