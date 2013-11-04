
package org.xbib.fst;

import org.xbib.fst.semiring.Semiring;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A mutable finite state transducer implementation.
 * <p/>
 * Holds an ArrayList of {@link State} objects allowing
 * additions/deletions.
 */
public class Fst {

    // fst states
    private List<State> states = null;
    // initial state
    protected State start;
    // input symbols map
    protected String[] inputSymbols;
    // output symbols map
    protected String[] outputSymbols;
    // semiring
    protected Semiring semiring;

    /**
     * Default Constructor
     */
    public Fst() {
        states = new ArrayList<State>();
    }

    /**
     * Constructor specifying the initial capacity of the states ArrayList (this
     * is an optimization used in various operations)
     *
     * @param numStates the initial capacity
     */
    public Fst(int numStates) {
        if (numStates > 0) {
            states = new ArrayList<State>(numStates);
        }
    }

    /**
     * Constructor specifying the fst's semiring
     *
     * @param s the fst's semiring
     */
    public Fst(Semiring s) {
        this();
        this.semiring = s;
    }

    /**
     * Get the initial states
     */
    public State getStart() {
        return start;
    }

    /**
     * Get the semiring
     */
    public Semiring getSemiring() {
        return semiring;
    }

    /**
     * Set the Semiring
     *
     * @param semiring the semiring to set
     */
    public void setSemiring(Semiring semiring) {
        this.semiring = semiring;
    }

    /**
     * Set the initial state
     *
     * @param start the initial state
     */
    public void setStart(State start) {
        this.start = start;
    }

    /**
     * Get the number of states in the fst
     */
    public int getNumStates() {
        return this.states.size();
    }

    public State getState(int index) {
        return states.get(index);
    }

    /**
     * Adds a state to the fst
     *
     * @param state the state to be added
     */
    public void addState(State state) {
        this.states.add(state);
        state.setId(states.size() - 1);
    }

    /**
     * Get the input symbols' array
     */
    public String[] getInputSymbols() {
        return inputSymbols;
    }

    /**
     * Set the input symbols
     *
     * @param inputSymbols the input symbols to set
     */
    public void setInputSymbols(String[] inputSymbols) {
        this.inputSymbols = inputSymbols;
    }

    /**
     * Get the output symbols' array
     */
    public String[] getOutputSymbols() {
        return outputSymbols;
    }

    /**
     * Set the output symbols
     *
     * @param outputSymbols the osyms to set
     */
    public void setOutputSymbols(String[] outputSymbols) {
        this.outputSymbols = outputSymbols;
    }

    /**
     * Serializes a symbol map to an ObjectOutputStream
     *
     * @param out the ObjectOutputStream. It should be already be initialized by
     *            the caller.
     * @param map the symbol map to write
     * @throws java.io.IOException
     */
    private void writeStringMap(ObjectOutputStream out, String[] map)
            throws IOException {
        out.writeInt(map.length);
        for (String aMap : map) {
            out.writeObject(aMap);
        }
    }

    /**
     * Serializes the current Fst instance to an ObjectOutputStream
     *
     * @param out the ObjectOutputStream. It should be already be initialized by
     *            the caller.
     * @throws java.io.IOException
     */
    private void writeFst(ObjectOutputStream out) throws IOException {
        writeStringMap(out, inputSymbols);
        writeStringMap(out, outputSymbols);
        out.writeInt(states.indexOf(start));

        out.writeObject(semiring);
        out.writeInt(states.size());

        HashMap<State, Integer> stateMap = new HashMap<State, Integer>(
                states.size(), 1.f);
        for (int i = 0; i < states.size(); i++) {
            State s = states.get(i);
            out.writeInt(s.getNumArcs());
            out.writeFloat(s.getFinalWeight());
            out.writeInt(s.getId());
            stateMap.put(s, i);
        }

        for (State s : states) {
            int numArcs = s.getNumArcs();
            for (int j = 0; j < numArcs; j++) {
                Arc a = s.getArc(j);
                out.writeInt(a.getInputLabel());
                out.writeInt(a.getOutputLabel());
                out.writeFloat(a.getWeight());
                out.writeInt(stateMap.get(a.getNextState()));
            }
        }
    }

    /**
     * Saves binary model
     *
     * @param out the binary model output stream
     * @throws java.io.IOException
     */
    public void saveModel(OutputStream out) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(out);
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        writeFst(oos);
        oos.flush();
        oos.close();
        bos.close();
    }

    /**
     * Deserializes a symbol map from an ObjectInputStream
     *
     * @param in the ObjectInputStream. It should be already be initialized by
     *           the caller.
     * @return the deserialized symbol map
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    protected static String[] readStringMap(ObjectInputStream in)
            throws IOException, ClassNotFoundException {

        int mapSize = in.readInt();
        String[] map = new String[mapSize];
        for (int i = 0; i < mapSize; i++) {
            String sym = (String) in.readObject();
            map[i] = sym;
        }

        return map;
    }

    /**
     * Deserializes an Fst from an ObjectInputStream
     *
     * @param in the ObjectInputStream. It should be already be initialized by
     *           the caller.
     * @return fst
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private static Fst readFst(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        String[] is = readStringMap(in);
        String[] os = readStringMap(in);
        int startid = in.readInt();
        Semiring semiring = (Semiring) in.readObject();
        int numStates = in.readInt();
        Fst res = new Fst(numStates);
        res.inputSymbols = is;
        res.outputSymbols = os;
        res.semiring = semiring;
        for (int i = 0; i < numStates; i++) {
            int numArcs = in.readInt();
            State s = new State(numArcs + 1);
            float f = in.readFloat();
            if (f == res.semiring.zero()) {
                f = res.semiring.zero();
            } else if (f == res.semiring.one()) {
                f = res.semiring.one();
            }
            s.setFinalWeight(f);
            s.setId(in.readInt());
            res.states.add(s);
        }
        res.setStart(res.states.get(startid));

        numStates = res.getNumStates();
        for (int i = 0; i < numStates; i++) {
            State s1 = res.getState(i);
            for (int j = 0; j < s1.initialNumArcs - 1; j++) {
                Arc a = new Arc();
                a.setInputLabel(in.readInt());
                a.setOutputLabel(in.readInt());
                a.setWeight(in.readFloat());
                a.setNextState(res.states.get(in.readInt()));
                s1.addArc(a);
            }
        }

        return res;
    }

    /**
     * Deserializes an Fst from disk
     *
     * @param in the binary model filename
     */
    public static Fst loadModel(InputStream in) throws IOException, ClassNotFoundException {
        Fst obj;
        BufferedInputStream bis = new BufferedInputStream(in);
        ObjectInputStream ois = new ObjectInputStream(bis);
        obj = readFst(ois);
        ois.close();
        bis.close();
        return obj;
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
        Fst other = (Fst) obj;
        if (!Arrays.equals(inputSymbols, other.inputSymbols)) {
            return false;
        }
        if (!Arrays.equals(outputSymbols, other.outputSymbols)) {
            return false;
        }
        if (start == null) {
            if (other.start != null) {
                return false;
            }
        } else if (!start.equals(other.start)) {
            return false;
        }
        if (states == null) {
            if (other.states != null) {
                return false;
            }
        } else if (!states.equals(other.states)) {
            return false;
        }
        if (semiring == null) {
            if (other.semiring != null) {
                return false;
            }
        } else if (!semiring.equals(other.semiring)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Fst(start=").append(start).append(", isyms=").append(inputSymbols).append(", osyms=").append(outputSymbols).append(", semiring=").append(semiring).append(")\n");
        for (State s : states) {
            sb.append("  ").append(s).append("\n");
            int numArcs = s.getNumArcs();
            for (int j = 0; j < numArcs; j++) {
                Arc a = s.getArc(j);
                sb.append("    ").append(a).append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Deletes a state
     *
     * @param state the state to delete
     */
    public void deleteState(State state) {
        if (state.getId() == this.start.getId()) {
            return;
        }

        this.states.remove(state);

        // delete arc's with nextstate equal to stateid
        ArrayList<Integer> toDelete;
        for (State s1 : states) {
            toDelete = new ArrayList<Integer>();
            int numArcs = s1.getNumArcs();
            for (int j = 0; j < numArcs; j++) {
                Arc a = s1.getArc(j);
                if (a.getNextState().equals(state)) {
                    toDelete.add(j);
                }
            }
            // indices not change when deleting in reverse ordering
            Object[] toDeleteArray = toDelete.toArray();
            Arrays.sort(toDeleteArray);
            for (int j = toDelete.size() - 1; j >= 0; j--) {
                Integer index = (Integer) toDeleteArray[j];
                s1.deleteArc(index);
            }
        }
    }

    /**
     * Remaps the states' ids.
     * <p/>
     * States' ids are renumbered starting from 0 up to
     */
    public void remapStateIds() {
        int numStates = states.size();
        for (int i = 0; i < numStates; i++) {
            states.get(i).setId(i);
        }

    }
}
