package org.xbib.fsa.moore.levenshtein;

import org.xbib.fsa.moore.AbstractAutomaton;
import org.xbib.fsa.moore.CompactState;
import org.xbib.util.FixedLengthBitSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Levenshtein automaton
 */
public final class LevenshteinAutomaton
        extends AbstractAutomaton<FixedLengthBitSet, Boolean> {

    private static final char NONALPHABET_CHARACTER = '$';

    private static final PositionState START_STATE = new PositionState(Arrays.asList(new Position[]{new Position(Parameter.I, Type.USUAL, 0, 0)}));

    private static final ChiType DEFAULT_CHI = ChiType.EPSILON;

    private int editDistance;

    public LevenshteinAutomaton name(String name) {
        setName(name);
        return this;
    }

    @Override
    public Set<FixedLengthBitSet> getAlphabet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Constructs a Levenshtein automaton
     *
     * @param editDistance maximum number of edits detected by this automaton
     */
    public LevenshteinAutomaton(int editDistance) {
        super();
        this.editDistance = editDistance;
        ChiType chi = DEFAULT_CHI;
        List<Transition> transitions = new LinkedList<Transition>();
        Map<PositionState, State<FixedLengthBitSet, Boolean>> stateMap = new HashMap<PositionState, State<FixedLengthBitSet, Boolean>>();
        LinkedList<PositionState> queue = new LinkedList<PositionState>();
        LinkedList<PositionState> added = new LinkedList<PositionState>();
        List<FixedLengthBitSet> powerset = buildPowerSet(2 * editDistance + 2);
        added.add(START_STATE);
        stateMap.put(START_STATE, getCurrentState());
        Set<State<FixedLengthBitSet, Boolean>> finishedstates = new HashSet();
        queue.add(START_STATE);
        while (!queue.isEmpty()) {
            PositionState state = queue.remove();
            for (FixedLengthBitSet b : powerset) {
                if (coversAllPositions(editDistance, b.fixedLength(), state)) {
                    PositionState nextstate = delta(chi, editDistance, state, b);
                    if (!nextstate.isEmpty()) {
                        int index = added.indexOf(nextstate);
                        if (index == -1) {
                            queue.add(nextstate);
                            added.add(nextstate);
                            State<FixedLengthBitSet, Boolean> s = new CompactState<FixedLengthBitSet, Boolean>(nextstate.first().getParameter() == Parameter.M ? Boolean.TRUE : null);
                            stateMap.put(nextstate, s);
                        } else {
                            nextstate = added.get(index);
                        }
                        transitions.add(new Transition(state, b, nextstate));
                        State<FixedLengthBitSet, Boolean> first = stateMap.get(state);
                        State<FixedLengthBitSet, Boolean> second = stateMap.get(nextstate);
                        if ((first != null) && (second != null)) {
                            finishedstates.add(first);
                            finishedstates.add(second);
                            first.addNextState(b, second);
                        }
                    }
                }
            }
        }
    }

    /**
     * Simultaneously traverses a DictionaryAutomaton and the provided
     * LevenshteinAutomaton to find all words within the specified Levenshtein
     * distance.
     *
     * @param input used to search for other string within an edit distance
     * @param dict  automaton representing all the words to search
     * @return Collection containing all the words within the edit distance
     *         matching the input word
     */
    public Set<CharSequence> recognize(CharSequence input, DictionaryAutomaton dict) {
        Set<CharSequence> result = new LinkedHashSet<CharSequence>();
        StringBuilder sb = new StringBuilder(input.length() + editDistance);
        for (int i = 0; i < editDistance; i++) {
            sb.append(NONALPHABET_CHARACTER);
        }
        sb.append(input);
        String s = sb.toString();
        Stack<RecognizeMapping> stack = new Stack<RecognizeMapping>();
        stack.push(new RecognizeMapping("", dict.getCurrentState(), this.getCurrentState(), 0));
        while (!stack.isEmpty()) {
            RecognizeMapping mapping = stack.pop();
            String ss = s.substring(mapping.index);
            for (Character c : dict.getAlphabet()) {
                State dictionarynext = mapping.dictstate.getNextState(c);
                if (dictionarynext != null) {
                    FixedLengthBitSet v = buildCharacteristicVector(c.charValue(), ss, editDistance);
                    State levenshteinnext = mapping.levenshteinstate.getNextState(v);
                    if (levenshteinnext != null) {
                        if (dictionarynext.isAccept() && levenshteinnext.isAccept()) {
                            result.add(mapping.work + c);
                        }
                        stack.push(new RecognizeMapping(mapping.work + c, dictionarynext, levenshteinnext, mapping.index + 1));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Generate a power set of binary set of the specified length.
     *
     * @param len length of the longest binary set
     * @return List of boolean arrays representing binary sets
     */
    private List<FixedLengthBitSet> buildPowerSet(int len) {
        int max = new Double(Math.pow(2.0, (double) len)).intValue();
        List<FixedLengthBitSet> result = new ArrayList<FixedLengthBitSet>(max);
        FixedLengthBitSet temp = new FixedLengthBitSet(len);
        result.add(temp);
        for (int i = 0; i < max - 1; i++) {
            temp = new FixedLengthBitSet(len);
            for (int j = 0; j < len; j++) {
                temp.set(j, result.get(i).get(j));
            }
            int j = len - 1;
            while (j >= 0) {
                temp.flip(j);
                if (!temp.get(j)) {
                    j--;
                } else {
                    break;
                }
            }
            result.add(temp);
        }
        if (len > 1) {
            result.addAll(buildPowerSet(len - 1));
        }
        return result;
    }

    @Override
    protected State<FixedLengthBitSet, Boolean> newState() {
        return new CompactState();
    }

    private static Position functionRM(PositionState state) {
        Position rm = null;
        for (Position pi : state) {
            if (pi.getType() == Type.USUAL) {
                rm = pi;
            }
        }
        for (Position pi : state) {
            if ((pi.getType() == Type.USUAL) && ((pi.getIndex() - pi.getError()) > (rm.getIndex() - rm.getError()))) {
                rm = pi;
            }
        }
        return rm;
    }

    private static PositionState functionM(int n, PositionState state, int k) {
        PositionState m = new PositionState();
        for (Position pi : state) {
            if (pi.getParameter() == Parameter.I) {
                m.add(new Position(Parameter.M, pi.getType(), pi.getIndex() + n + 1 - k, pi.getError()));
            } else {
                m.add(new Position(Parameter.I, pi.getType(), pi.getIndex() - n - 1 + k, pi.getError()));
            }
        }
        return m;
    }

    private static boolean functionF(int n, Position pos, int k) {
        if (pos == null) {
            return false;
        }
        if (pos.getParameter() == Parameter.I) {
            return ((k <= (2 * n + 1)) && (pos.getError() <= (pos.getIndex() + 2 * n + 1 - k)));
        }
        return (pos.getError() > (pos.getIndex() + n));
    }

    private static Set<Point> deltaED(ChiType chi, int n, Point point, FixedLengthBitSet b) {
        int index = point.getX();
        int error = point.getY();
        switch (chi) {
            case EPSILON:
                if (b.fixedLength() == 0) {
                    if (error < n) {
                        return Point.newSet(
                                new Point(Type.USUAL, index, error + 1));

                    }
                    return Point.emptySet();
                }
                if (b.get(0)) {
                    return Point.newSet(
                            new Point(Type.USUAL, index + 1, error));
                }
                if (b.fixedLength() == 1) {
                    if (error < n) {
                        return Point.newSet(
                                new Point(Type.USUAL, index, error + 1),
                                new Point(Type.USUAL, index + 1, error + 1));
                    }
                    return Point.emptySet();
                }
                int posFirst = 0;
                for (int i = 1; i < b.fixedLength(); i++) {
                    if (b.get(i)) {
                        posFirst = i;
                        break;
                    }
                }
                if (posFirst == 0) {
                    return Point.newSet(
                            new Point(Type.USUAL, index, error + 1),
                            new Point(Type.USUAL, index + 1, error + 1));
                }
                posFirst++;
                return Point.newSet(
                        new Point(Type.USUAL, index, error + 1),
                        new Point(Type.USUAL, index + 1, error + 1),
                        new Point(Type.USUAL, index + posFirst, error + posFirst - 1));

            case T:
                if (point.getType() == Type.T) {
                    if (b.get(0)) {
                        return Point.newSet(
                                new Point(Type.USUAL, index + 2, error)
                        );
                    }
                    return Point.emptySet();
                }
                if (b.fixedLength() == 0) {
                    if (error < n) {
                        return Point.newSet(
                                new Point(Type.USUAL, index, error + 1)
                        );
                    }
                    return Point.emptySet();
                }
                if (b.get(b.fixedLength() - 1)) {
                    return Point.newSet(
                            new Point(Type.USUAL, index + 1, error)
                    );

                }
                if (b.fixedLength() == 1) {
                    if (error < n) {
                        return Point.newSet(
                                new Point(Type.USUAL, index, error + 1),
                                new Point(Type.USUAL, index + 1, error + 1)
                        );
                    }
                    return Point.emptySet();
                }
                if (b.get(1)) {
                    return Point.newSet(
                            new Point(Type.USUAL, index, error + 1),
                            new Point(Type.USUAL, index + 1, error + 1),
                            new Point(Type.USUAL, index + 2, error + 1),
                            new Point(Type.T, index, error + 1)
                    );
                }
                posFirst = b.fixedLength() - 2;
                while ((posFirst >= 0) && !b.get(posFirst)) {
                    posFirst--;
                }
                if (posFirst == -1) {
                    return Point.newSet(
                            new Point(Type.USUAL, index, error + 1),
                            new Point(Type.USUAL, index + 1, error + 1)
                    );
                }
                posFirst += 2;
                return Point.newSet(
                        new Point(Type.USUAL, index, error + 1),
                        new Point(Type.USUAL, index + 1, error + 1),
                        new Point(Type.USUAL, index + posFirst, error + posFirst - 1)
                );

            case MS:
                if (point.getType() == Type.MS) {
                    return Point.newSet(
                            new Point(Type.USUAL, index + 1, error)
                    );
                }
                if (b.fixedLength() == 0) {
                    if (error < n) {
                        return Point.newSet(
                                new Point(Type.USUAL, index, error + 1)
                        );
                    }
                    return Point.emptySet();
                }
                if (b.fixedLength() == 1) {
                    if (error < n) {
                        return Point.newSet(
                                new Point(Type.USUAL, index, error + 1),
                                new Point(Type.USUAL, index + 1, error + 1),
                                new Point(Type.MS, index, error + 1)
                        );
                    }
                }
                return Point.newSet(
                        new Point(Type.USUAL, index, error + 1),
                        new Point(Type.USUAL, index + 1, error + 1),
                        new Point(Type.USUAL, index + 2, error + 1),
                        new Point(Type.MS, index, error + 1)
                );
        }
        return null;
    }

    private static FixedLengthBitSet functionR(int n, Position pos, FixedLengthBitSet b) {
        int length;
        if (pos.getParameter() == Parameter.I) {
            if ((n - pos.getError() + 1) < (b.fixedLength() - n - pos.getIndex())) {
                length = n - pos.getError() + 1;
            } else {
                length = b.fixedLength() - n - pos.getIndex();
            }
            int start = n + pos.getIndex();
            int revStart = b.fixedLength() - start - length;
            FixedLengthBitSet result = b.subset(revStart, length);
            return result;
        }
        if ((n - pos.getError() + 1) < -pos.getIndex()) {
            length = n - pos.getError() + 1;
        } else {
            length = -pos.getIndex();
        }
        int start = b.fixedLength() + pos.getIndex();
        int revStart = b.fixedLength() - start - length;
        FixedLengthBitSet result = b.subset(revStart, length);
        return result;
    }

    private static PositionState deltaE(ChiType chi, int n, Position q, FixedLengthBitSet b) {
        FixedLengthBitSet h = functionR(n, q, b);
        Set<Point> delta_ed = deltaED(chi, n, new Point(q.getType(), q.getIndex(), q.getError()), h);
        if (delta_ed.isEmpty()) {
            return new PositionState();
        }
        PositionState state = new PositionState();
        if (q.getParameter() == Parameter.I) {
            for (Point pi : delta_ed) {
                state.add(new Position(Parameter.I, pi.getType(), pi.getX() - 1, pi.getY()));
            }
        } else {
            for (Point pi : delta_ed) {
                state.add(new Position(Parameter.M, pi.getType(), pi.getX(), pi.getY()));
            }
        }
        return state;
    }

    private static PositionState delta(ChiType chi, int n, PositionState state, FixedLengthBitSet b) {
        PositionState next = new PositionState();
        boolean add;
        for (Position q : state) {
            PositionState delta_e = deltaE(chi, n, q, b);
            if (!delta_e.isEmpty()) {
                for (Position pi : delta_e) {
                    add = true;
                    Iterator<Position> iter = next.iterator();
                    while (iter.hasNext()) {
                        Position p = iter.next();
                        if (lessThanSubsume(pi, p)) {
                            iter.remove();
                        } else {
                            if (p.equals(pi) || lessThanSubsume(p, pi)) {
                                add = false;
                                break;
                            }
                        }
                    }
                    if (add == true) {
                        next.add(pi);
                    }
                }
            }
        }
        if (functionF(n, functionRM(next), b.fixedLength())) {
            next = functionM(n, next, b.fixedLength());
        }
        return next;
    }

    private static boolean lessThanSubsume(Position q1, Position q2) {
        if ((q1.getType() != Type.USUAL) || (q2.getError() <= q1.getError())) {
            return false;
        }
        int m;
        if (q2.getType() == Type.T) {
            m = q2.getIndex() + 1 - q1.getIndex();
        } else {
            m = q2.getIndex() - q1.getIndex();
        }
        if (m < 0) {
            m = -m;
        }
        return (m <= (q2.getError() - q1.getError()));
    }

    private static boolean coversAllPositions(int edit_distance, int string_length, PositionState state) {
        Position pos = state.first();
        if (pos.getParameter() == Parameter.I) {
            if (state.equals(START_STATE)) {
                return (string_length >= (pos.getIndex() + edit_distance));
            } else {
                for (Position pi : state) {
                    if (string_length < (2 * edit_distance + pi.getIndex() - pi.getError() + 1)) {
                        return false;
                    }
                }
            }
        } else {
            Position q;
            if (string_length < edit_distance) {
                q = new Position(Parameter.M, Type.USUAL, 0, edit_distance - string_length);
            } else {
                q = new Position(Parameter.M, Type.USUAL, edit_distance - string_length, 0);
            }

            for (Position pi : state) {
                if (!pi.equals(q) && !lessThanSubsume(q, pi)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Creates a characteristic vector of a character against a string.
     *
     * @param c Character from which the characteristic vector is created
     * @param s String from which the characteristic vector is created
     * @param n Distance greater than or equal to one of the
     *          desired LevenshteinAutomaton
     * @return FixedLengthBitSet representing the characteristic bit vector
     */
    private static FixedLengthBitSet buildCharacteristicVector(char c, String s, int n) {
        int len = 2 * n + 2;
        if (s.length() < len) {
            len = s.length();
        }
        FixedLengthBitSet result = new FixedLengthBitSet(len);
        int firstBit = len - 1;
        for (int i = 0; i < len; i++) {
            result.set((firstBit - i), (c == s.charAt(i)));
        }
        return result;
    }

    private static class RecognizeMapping {

        String work;
        State dictstate;
        State levenshteinstate;
        int index;

        public RecognizeMapping(String work, State dictionarystate, State levenshteinstate,
                                int index) {
            this.work = work;
            this.dictstate = dictionarystate;
            this.levenshteinstate = levenshteinstate;
            this.index = index;
        }
    }
}
