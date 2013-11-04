
package org.xbib.fst.operations;

import org.xbib.fst.Arc;
import org.xbib.fst.Fst;
import org.xbib.fst.ImmutableFst;
import org.xbib.fst.State;

/**
 * Project operation.
 */
public class Project {

    public enum Type {
        INPUT, OUTPUT
    }

    /**
     * Default Constructor
     */
    private Project() {
    }

    /**
     * Projects an fst onto its domain or range by either copying each arc's
     * input label to its output label or vice versa.
     *
     * @param fst
     * @param pType
     */
    public static void apply(Fst fst, Type pType) {
        if (pType == Type.INPUT) {
            fst.setOutputSymbols(fst.getInputSymbols());
        } else if (pType == Type.OUTPUT) {
            fst.setInputSymbols(fst.getOutputSymbols());
        }

        int numStates = fst.getNumStates();
        for (int i = 0; i < numStates; i++) {
            State s = fst.getState(i);
            // Immutable fsts hold an additional (null) arc
            int numArcs = (fst instanceof ImmutableFst) ? s.getNumArcs() - 1 : s
                    .getNumArcs();
            for (int j = 0; j < numArcs; j++) {
                Arc a = s.getArc(j);
                if (pType == Type.INPUT) {
                    a.setOutputLabel(a.getInputLabel());
                } else if (pType == Type.OUTPUT) {
                    a.setInputLabel(a.getOutputLabel());
                }
            }
        }
    }
}
