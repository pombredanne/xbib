
package org.xbib.fst.operations;

import org.xbib.fst.Arc;
import org.xbib.fst.Fst;
import org.xbib.fst.State;
import org.xbib.fst.semiring.Semiring;

/**
 * Reverse operation.
 */
public class Reverse {
    /**
     * Default Constructor
     */
    private Reverse() {
    }

    /**
     * Reverses an fst
     *
     * @param fst the fst to reverse
     * @return the reversed fst
     */
    public static Fst get(Fst fst) {
        if (fst.getSemiring() == null) {
            return null;
        }

        ExtendFinal.apply(fst);

        Semiring semiring = fst.getSemiring();

        Fst res = new Fst(fst.getNumStates());
        res.setSemiring(semiring);

        res.setInputSymbols(fst.getOutputSymbols());
        res.setOutputSymbols(fst.getInputSymbols());

        State[] stateMap = new State[fst.getNumStates()];
        int numStates = fst.getNumStates();
        for (int i = 0; i < numStates; i++) {
            State is = fst.getState(i);
            State s = new State(semiring.zero());
            res.addState(s);
            stateMap[is.getId()] = s;
            if (is.getFinalWeight() != semiring.zero()) {
                res.setStart(s);
            }
        }

        stateMap[fst.getStart().getId()].setFinalWeight(semiring.one());

        for (int i = 0; i < numStates; i++) {
            State olds = fst.getState(i);
            State news = stateMap[olds.getId()];
            int numArcs = olds.getNumArcs();
            for (int j = 0; j < numArcs; j++) {
                Arc olda = olds.getArc(j);
                State next = stateMap[olda.getNextState().getId()];
                Arc newa = new Arc(olda.getInputLabel(), olda.getOutputLabel(),
                        semiring.reverse(olda.getWeight()), news);
                next.addArc(newa);
            }
        }

        ExtendFinal.undo(fst);
        return res;
    }
}
