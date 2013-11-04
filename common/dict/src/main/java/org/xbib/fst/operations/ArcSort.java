package org.xbib.fst.operations;

import org.xbib.fst.Arc;
import org.xbib.fst.Fst;

import java.util.Comparator;

/**
 * ArcSort operation.
 */
public class ArcSort {
    /**
     * Default Constructor
     */
    private ArcSort() {
    }

    /**
     * Applies the ArcSort on the provided fst. Sorting can be applied either on
     * input or output label based on the provided comparator.
     *
     * @param fst the fst to sort it's arcs
     * @param cmp the provided Comparator
     */
    public static void apply(Fst fst, Comparator<Arc> cmp) {
        for (int i = 0; i < fst.getNumStates(); i++) {
            fst.getState(i).arcSort(cmp);
        }
    }
}
