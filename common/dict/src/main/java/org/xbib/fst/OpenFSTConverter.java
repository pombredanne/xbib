
package org.xbib.fst;

import org.xbib.fst.semiring.Semiring;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides the required functionality in order to convert from/to openfst's
 * text format
 */
public class OpenFSTConverter {

    /**
     * Default private Constructor.
     */
    private OpenFSTConverter() {
    }

    /**
     * Exports an fst to the openfst text format Several files are created as
     * follows: - basename.input.syms - basename.output.syms - basename.fst.txt
     * See <a href="http://www.openfst.org/twiki/bin/view/FST/FstQuickTour">OpenFst
     * Quick Tour</a>
     *
     * @param fst      the fst to export
     * @param basename the files' base name
     */
    public static void export(Fst fst, String basename) throws IOException {
        exportSymbols(fst.getInputSymbols(), new FileWriter(basename + ".input.syms"));
        exportSymbols(fst.getOutputSymbols(), new FileWriter(basename + ".output.syms"));
        exportFst(fst, new FileWriter(basename + ".fst.txt"));
    }

    /**
     * Exports an fst to the openfst text format
     *
     * @param fst  the fst to export
     * @param file the openfst's fst.txt filename
     */
    private static void exportFst(Fst fst, Writer file) {
        PrintWriter out = new PrintWriter(file);

        // print start first
        State start = fst.getStart();
        out.println(start.getId() + "\t" + start.getFinalWeight());

        // print all states
        int numStates = fst.getNumStates();
        for (int i = 0; i < numStates; i++) {
            State s = fst.getState(i);
            if (s.getId() != fst.getStart().getId()) {
                out.println(s.getId() + "\t" + s.getFinalWeight());
            }
        }

        String[] isyms = fst.getInputSymbols();
        String[] osyms = fst.getOutputSymbols();
        numStates = fst.getNumStates();
        for (int i = 0; i < numStates; i++) {
            State s = fst.getState(i);
            int numArcs = s.getNumArcs();
            for (int j = 0; j < numArcs; j++) {
                Arc arc = s.getArc(j);
                String isym = (isyms != null) ? isyms[arc.getInputLabel()]
                        : Integer.toString(arc.getInputLabel());
                String osym = (osyms != null) ? osyms[arc.getOutputLabel()]
                        : Integer.toString(arc.getOutputLabel());

                out.println(s.getId() + "\t" + arc.getNextState().getId()
                        + "\t" + isym + "\t" + osym + "\t"
                        + arc.getWeight());
            }
        }

        out.close();
    }

    /**
     * Exports a symbols' map to the openfst text format
     *
     * @param syms   the symbols' map
     * @param writer the the openfst's symbols filename
     */
    private static void exportSymbols(String[] syms, Writer writer) throws IOException {
        if (syms == null) {
            return;
        }
        PrintWriter out = new PrintWriter(writer);
        for (int i = 0; i < syms.length; i++) {
            String key = syms[i];
            out.println(key + "\t" + i);
        }
        out.close();
    }

    /**
     * Imports an openfst's symbols file
     *
     * @param fis the symbols' input stream
     * @return Map containing the impprted string-to-id mapping
     */
    private static Map<String, Integer> importSymbols(InputStream fis) throws IOException {
        DataInputStream dis = new DataInputStream(fis);
        BufferedReader br = new BufferedReader(new InputStreamReader(dis));
        Map<String, Integer> syms = new HashMap<String, Integer>();
        String strLine;
        while ((strLine = br.readLine()) != null) {
            String[] tokens = strLine.split("\\t");
            String sym = tokens[0];
            Integer index = Integer.parseInt(tokens[1]);
            syms.put(sym, index);

        }
        return syms;
    }

    /**
     * Imports an openfst text format Several files are imported as follows: -
     * basename.input.syms - basename.output.syms - basename.fst.txt
     *
     * @param basename the files' base name
     * @param semiring the fst's semiring
     */
    public static Fst importFst(String basename, Semiring semiring) throws IOException {
        Fst fst = new Fst(semiring);

        Map<String, Integer> isyms = importSymbols(new FileInputStream(basename + ".input.syms"));
        if (isyms == null) {
            isyms = new HashMap<String, Integer>();
            isyms.put("<eps>", 0);
        }

        Map<String, Integer> osyms = importSymbols(new FileInputStream(basename + ".output.syms"));
        if (osyms == null) {
            osyms = new HashMap<String, Integer>();
            osyms.put("<eps>", 0);
        }

        Map<String, Integer> ssyms = importSymbols(new FileInputStream(basename + ".states.syms"));

        // Parse input
        FileInputStream fis = new FileInputStream(basename + ".fst.txt");

        DataInputStream dis = new DataInputStream(fis);
        BufferedReader br = new BufferedReader(new InputStreamReader(dis));
        boolean firstLine = true;
        String strLine;
        HashMap<Integer, State> stateMap = new HashMap<Integer, State>();

        while ((strLine = br.readLine()) != null) {
            String[] tokens = strLine.split("\\t");
            Integer inputStateId;
            if (ssyms == null) {
                inputStateId = Integer.parseInt(tokens[0]);
            } else {
                inputStateId = ssyms.get(tokens[0]);
            }
            State inputState = stateMap.get(inputStateId);
            if (inputState == null) {
                inputState = new State(semiring.zero());
                fst.addState(inputState);
                stateMap.put(inputStateId, inputState);
            }

            if (firstLine) {
                firstLine = false;
                fst.setStart(inputState);
            }

            if (tokens.length > 2) {
                Integer nextStateId;
                if (ssyms == null) {
                    nextStateId = Integer.parseInt(tokens[1]);
                } else {
                    nextStateId = ssyms.get(tokens[1]);
                }

                State nextState = stateMap.get(nextStateId);
                if (nextState == null) {
                    nextState = new State(semiring.zero());
                    fst.addState(nextState);
                    stateMap.put(nextStateId, nextState);
                }
                // Adding arc
                if (isyms.get(tokens[2]) == null) {
                    isyms.put(tokens[2], isyms.size());
                }
                int iLabel = isyms.get(tokens[2]);
                if (osyms.get(tokens[3]) == null) {
                    osyms.put(tokens[3], osyms.size());
                }
                int oLabel = osyms.get(tokens[3]);
                float arcWeight = Float.parseFloat(tokens[4]);
                Arc arc = new Arc(iLabel, oLabel, arcWeight, nextState);
                inputState.addArc(arc);
            } else {
                // This is a final weight
                float finalWeight = Float.parseFloat(tokens[1]);
                inputState.setFinalWeight(finalWeight);
            }
        }
        dis.close();
        fst.setInputSymbols(toStringArray(isyms));
        fst.setOutputSymbols(toStringArray(osyms));

        return fst;
    }

    public static String[] toStringArray(Map<String, Integer> syms) {
        String[] res = new String[syms.size()];
        for (String sym : syms.keySet()) {
            res[syms.get(sym)] = sym;
        }
        return res;
    }
}
