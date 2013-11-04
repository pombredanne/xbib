package org.xbib.re;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class EquivMap {

    private final List<CharSet> csets = Generics.newArrayList();
    private final int size;
    private final int[] ascii = new int[128];

    public EquivMap(State start) {
        Arrays.fill(ascii, 0);
        split(start, new HashSet<State>());
        Collections.sort(csets, CardinalityComparator.INSTANCE);
        size = csets.size() + 1;

        for (int i = 0; i < csets.size(); i++) {
            CharSet cset = csets.get(i);
            for (int c = cset.nextChar(0); c >= 0 && c < 128; c = cset.nextChar(c + 1)) {
                ascii[c] = i + 1;
            }
        }
    }

    public int size() {
        return size;
    }

    public int getIndex(char c) {
        if (c < 128) {
            return ascii[c];
        }
        // TODO: remove ascii-only charsets
        // TODO: better data structure?
        for (int i = 0; i < size; i++) {
            if (csets.get(i).contains(c)) {
                return i + 1;
            }
        }
        return 0;
    }

    private void split(State state, Set<State> mark) {
        if (state == null || mark.contains(state)) {
            return;
        }
        mark.add(state);
        if (state.getCharSet() != null) {
            CharSet cset = state.getCharSet();
            for (int i = 0, size = csets.size(); i < size && !cset.isEmpty(); i++) {
                CharSet existing = csets.get(i);
                CharSet intersect = existing.intersect(cset);
                if (!intersect.isEmpty()) {
                    cset = cset.subtract(intersect);
                    CharSet subtract = existing.subtract(intersect);
                    if (subtract.isEmpty()) {
                        csets.set(i, intersect);
                    } else {
                        csets.set(i, subtract);
                        csets.add(intersect);
                    }
                }
            }
            if (!cset.isEmpty()) {
                csets.add(cset);
            }
        }
        split(state.getState1(), mark);
        split(state.getState2(), mark);
    }
}
