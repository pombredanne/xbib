
package org.xbib.fst.operations;

import org.xbib.fst.Arc;
import org.xbib.fst.Fst;
import org.xbib.fst.State;
import org.xbib.fst.semiring.Semiring;

import java.util.ArrayList;

/**
 * Extend an Fst to a single final state and undo operations.
 */
public class ExtendFinal {

    /**
     * Default Contructor
     */
    private ExtendFinal() {
    }

    /**
     * Extends an Fst to a single final state.
     * <p/>
     * It adds a new final state with a 0.0 (Semiring's 1) final wight and
     * connects the current final states to it using epsilon transitions with
     * weight equal to the original final state's weight.
     *
     * @param fst the Fst to extend
     */
    public static void apply(Fst fst) {
        Semiring semiring = fst.getSemiring();
        ArrayList<State> fStates = new ArrayList<State>();

        int numStates = fst.getNumStates();
        for (int i = 0; i < numStates; i++) {
            State s = fst.getState(i);
            if (s.getFinalWeight() != semiring.zero()) {
                fStates.add(s);
            }
        }

        // Add a new single final
        State newFinal = new State(semiring.one());
        fst.addState(newFinal);
        for (State s : fStates) {
            // add epsilon transition from the old final to the new one
            s.addArc(new Arc(0, 0, s.getFinalWeight(), newFinal));
            // set old state's weight to zero
            s.setFinalWeight(semiring.zero());
        }
    }

    /**
     * Undo of the extend operation
     */
    public static void undo(Fst fst) {
        State f = null;
        int numStates = fst.getNumStates();
        for (int i = 0; i < numStates; i++) {
            State s = fst.getState(i);
            if (s.getFinalWeight() != fst.getSemiring().zero()) {
                f = s;
                break;
            }
        }
        if (f == null) {
            return;
        }
        for (int i = 0; i < numStates; i++) {
            State s = fst.getState(i);
            for (int j = 0; j < s.getNumArcs(); j++) {
                Arc a = s.getArc(j);
                if (a.getInputLabel() == 0 && a.getOutputLabel() == 0
                        && a.getNextState().getId() == f.getId()) {
                    s.setFinalWeight(a.getWeight());
                }
            }
        }
        fst.deleteState(f);
    }

}
