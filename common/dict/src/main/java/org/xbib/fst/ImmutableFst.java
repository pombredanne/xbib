
package org.xbib.fst;

import org.xbib.fst.semiring.Semiring;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * An immutable finite state transducer implementation.
 * <p/>
 * Holds a fixed size array of {@link ImmutableState} objects
 * not allowing additions/deletions
 */
public class ImmutableFst extends Fst {

    // fst states
    private ImmutableState[] states = null;
    // number of states
    private int numStates;

    /**
     * Default private constructor.
     * <p/>
     * An ImmutableFst cannot be created directly. It needs to be deserialized.
     *
     * @see ImmutableFst#loadModel(String)
     */
    private ImmutableFst() {
    }

    /**
     * Private Constructor specifying the capacity of the states array
     * <p/>
     * An ImmutableFst cannot be created directly. It needs to be deserialized.
     *
     * @param numStates the number of fst's states
     * @see ImmutableFst#loadModel(String)
     */
    private ImmutableFst(int numStates) {
        super(0);
        this.numStates = numStates;
        this.states = new ImmutableState[numStates];
    }

    @Override
    public int getNumStates() {
        return this.numStates;
    }

    @Override
    public ImmutableState getState(int index) {
        return states[index];
    }

    @Override
    public void addState(State state) {
        throw new IllegalArgumentException("You cannot modify an immutable FST");
    }

    @Override
    public void saveModel(OutputStream out) throws IOException {
        throw new IllegalArgumentException("You cannot save an immutable FST");
    }

    /**
     * Deserializes an ImmutableFst from an ObjectInputStream
     *
     * @param in the ObjectInputStream. It should be already be initialized by
     *           the caller.
     * @return ImmutableFst
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    private static ImmutableFst readImmutableFst(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        String[] is = readStringMap(in);
        String[] os = readStringMap(in);
        int startid = in.readInt();
        Semiring semiring = (Semiring) in.readObject();
        int numStates = in.readInt();
        ImmutableFst res = new ImmutableFst(numStates);
        res.inputSymbols = is;
        res.outputSymbols = os;
        res.semiring = semiring;
        for (int i = 0; i < numStates; i++) {
            int numArcs = in.readInt();
            ImmutableState s = new ImmutableState(numArcs + 1);
            float f = in.readFloat();
            if (f == res.semiring.zero()) {
                f = res.semiring.zero();
            } else if (f == res.semiring.one()) {
                f = res.semiring.one();
            }
            s.setFinalWeight(f);
            s.setId(in.readInt());
            res.states[s.getId()] = s;
        }
        res.setStart(res.states[startid]);

        numStates = res.states.length;
        for (int i = 0; i < numStates; i++) {
            ImmutableState s1 = res.states[i];
            for (int j = 0; j < s1.initialNumArcs - 1; j++) {
                Arc a = new Arc();
                a.setInputLabel(in.readInt());
                a.setOutputLabel(in.readInt());
                a.setWeight(in.readFloat());
                a.setNextState(res.states[in.readInt()]);
                s1.setArc(j, a);
            }
        }

        return res;
    }

    /**
     * Deserializes an ImmutableFst from an InputStream
     *
     * @param inputStream the InputStream. It should be already be initialized
     *                    by the caller.
     */
    public static ImmutableFst loadModel(InputStream inputStream) throws IOException, ClassNotFoundException {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ObjectInputStream ois = new ObjectInputStream(bis);
        ImmutableFst obj = readImmutableFst(ois);
        ois.close();
        bis.close();
        inputStream.close();

        return obj;
    }

    /**
     * Deserializes an ImmutableFst from disk
     *
     * @param filename the binary model filename
     */
    public static ImmutableFst loadModel(String filename) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filename);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ObjectInputStream ois = new ObjectInputStream(bis);
        ImmutableFst obj = readImmutableFst(ois);
        ois.close();
        bis.close();
        fis.close();
        return obj;
    }

    @Override
    public void deleteState(State state) {
        throw new IllegalArgumentException("You cannot modify an ImmutableFst.");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Fst(start=").append(start).append(", isyms=").append(Arrays.toString(inputSymbols))
                .append(", osyms=").append(Arrays.toString(outputSymbols))
                .append(", semiring=").append(semiring).append(")\n");
        int numStates = states.length;
        for (int i = 0; i < numStates; i++) {
            State s = states[i];
            sb.append("  ").append(s).append("\n");
            int numArcs = s.getNumArcs();
            for (int j = 0; j < numArcs; j++) {
                Arc a = s.getArc(j);
                sb.append("    ").append(a).append("\n");
            }
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ImmutableFst other = (ImmutableFst) obj;
        if (!Arrays.equals(states, other.states)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        return true;
    }
}
