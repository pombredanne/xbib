
package org.xbib.fst.operations;

import org.xbib.fst.Arc;

import java.util.Comparator;

/**
 * Comparator used in {@link ArcSort} for sorting
 * based on output labels
 */
public class OutputLabelComparator implements Comparator<Arc> {

    @Override
    public int compare(Arc o1, Arc o2) {
        if (o1 == null) {
            return 1;
        }
        if (o2 == null) {
            return -1;
        }
        return (o1.getOutputLabel() < o2.getOutputLabel()) ? -1 : ((o1.getOutputLabel() == o2
                .getOutputLabel()) ? 0 : 1);
    }

}
