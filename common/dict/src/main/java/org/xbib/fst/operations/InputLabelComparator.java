
package org.xbib.fst.operations;

import org.xbib.fst.Arc;

import java.util.Comparator;

/**
 * Comparator used in {@link ArcSort} for sorting based on input labels
 */
public class InputLabelComparator implements Comparator<Arc> {

    @Override
    public int compare(Arc o1, Arc o2) {
        if (o1 == null) {
            return 1;
        }
        if (o2 == null) {
            return -1;
        }
        return (o1.getInputLabel() < o2.getInputLabel()) ? -1 : ((o1.getInputLabel() == o2.getInputLabel()) ? 0 : 1);
    }
}
