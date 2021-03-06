
package org.xbib.fst.operations;

import org.xbib.fst.Arc;
import org.xbib.fst.Fst;
import org.xbib.fst.Pair;
import org.xbib.fst.State;
import org.xbib.fst.semiring.Semiring;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Determinize operation.
 * <p/>
 * See: M. Mohri, "Finite-State Transducers in Language and Speech Processing",
 * Computational Linguistics, 23:2, 1997.
 */
public class Determinize {

    /**
     * Default constructor
     */
    private Determinize() {
    }

    private static Pair<State, Float> getPair(
            ArrayList<Pair<State, Float>> queue, State state, Float zero) {
        Pair<State, Float> res = null;
        for (Pair<State, Float> tmp : queue) {
            if (state.getId() == tmp.getLeft().getId()) {
                res = tmp;
                break;
            }
        }

        if (res == null) {
            res = new Pair<State, Float>(state, zero);
            queue.add(res);
        }

        return res;
    }

    private static ArrayList<Integer> getUniqueLabels(Fst fst,
                                                      ArrayList<Pair<State, Float>> pa) {
        ArrayList<Integer> res = new ArrayList<Integer>();

        for (Pair<State, Float> p : pa) {
            State s = p.getLeft();

            int numArcs = s.getNumArcs();
            for (int j = 0; j < numArcs; j++) {
                Arc arc = s.getArc(j);
                if (!res.contains(arc.getInputLabel())) {
                    res.add(arc.getInputLabel());
                }
            }
        }
        return res;
    }

    private static State getStateLabel(ArrayList<Pair<State, Float>> pa,
                                       HashMap<String, State> stateMapper) {
        StringBuilder sb = new StringBuilder();
        for (Pair<State, Float> p : pa) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append("(").append(p.getLeft()).append(",").append(p.getRight()).append(")");
        }
        return stateMapper.get(sb.toString());
    }

    /**
     * Determinizes an fst. The result will be an equivalent fst that has the
     * property that no state has two transitions with the same input label. For
     * this algorithm, epsilon transitions are treated as regular symbols.
     *
     * @param fst the fst to determinize
     * @return the determinized fst
     */
    public static Fst get(Fst fst) {

        if (fst.getSemiring() == null) {
            // semiring not provided
            return null;
        }

        // initialize the queue and new fst
        Semiring semiring = fst.getSemiring();
        Fst res = new Fst(semiring);
        res.setInputSymbols(fst.getInputSymbols());
        res.setOutputSymbols(fst.getOutputSymbols());

        // stores the queue (item in index 0 is next)
        ArrayList<ArrayList<Pair<State, Float>>> queue = new ArrayList<ArrayList<Pair<State, Float>>>();

        HashMap<String, State> stateMapper = new HashMap<String, State>();

        State s = new State(semiring.zero());
        String stateString = "(" + fst.getStart() + "," + semiring.one() + ")";
        queue.add(new ArrayList<Pair<State, Float>>());
        queue.get(0)
                .add(new Pair<State, Float>(fst.getStart(), semiring.one()));
        res.addState(s);
        stateMapper.put(stateString, s);
        res.setStart(s);

        while (queue.size() > 0) {
            ArrayList<Pair<State, Float>> p = queue.get(0);
            State pnew = getStateLabel(p, stateMapper);
            queue.remove(0);
            ArrayList<Integer> labels = getUniqueLabels(fst, p);
            for (int label : labels) {
                Float wnew = semiring.zero();
                // calc w'
                for (Pair<State, Float> ps : p) {
                    State old = ps.getLeft();
                    Float u = ps.getRight();
                    int numArcs = old.getNumArcs();
                    for (int j = 0; j < numArcs; j++) {
                        Arc arc = old.getArc(j);
                        if (label == arc.getInputLabel()) {
                            wnew = semiring.plus(wnew,
                                    semiring.times(u, arc.getWeight()));
                        }
                    }
                }

                // calc new states
                // keep residual weights to variable forQueue
                ArrayList<Pair<State, Float>> forQueue = new ArrayList<Pair<State, Float>>();
                for (Pair<State, Float> ps : p) {
                    State old = ps.getLeft();
                    Float u = ps.getRight();
                    Float wnewRevert = semiring.divide(semiring.one(), wnew);
                    int numArcs = old.getNumArcs();
                    for (int j = 0; j < numArcs; j++) {
                        Arc arc = old.getArc(j);
                        if (label == arc.getInputLabel()) {
                            State oldstate = arc.getNextState();
                            Pair<State, Float> pair = getPair(forQueue,
                                    oldstate, semiring.zero());
                            pair.setRight(semiring.plus(
                                    pair.getRight(),
                                    semiring.times(wnewRevert,
                                            semiring.times(u, arc.getWeight()))));
                        }
                    }
                }

                // build new state's id and new elements for queue
                String qnewid = "";
                for (Pair<State, Float> ps : forQueue) {
                    State old = ps.getLeft();
                    Float unew = ps.getRight();
                    if (!qnewid.equals("")) {
                        qnewid = qnewid + ",";
                    }
                    qnewid = qnewid + "(" + old + "," + unew + ")";
                }

                if (stateMapper.get(qnewid) == null) {
                    State qnew = new State(semiring.zero());
                    res.addState(qnew);
                    stateMapper.put(qnewid, qnew);
                    // update new state's weight
                    Float fw = qnew.getFinalWeight();
                    for (Pair<State, Float> ps : forQueue) {
                        fw = semiring.plus(fw, semiring.times(ps.getLeft()
                                .getFinalWeight(), ps.getRight()));
                    }
                    qnew.setFinalWeight(fw);

                    queue.add(forQueue);
                }
                pnew.addArc(new Arc(label, label, wnew, stateMapper.get(qnewid)));
            }
        }

        return res;
    }
}
