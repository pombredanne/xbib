package org.xbib.analyzer.elements.mab;

import org.xbib.elements.marc.extensions.mab.MABBuilder;
import org.xbib.elements.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class Leader extends MABElement {

    private final static MABElement element = new Leader();

    private Leader() {
    }

    public static MABElement getInstance() {
        return element;
    }

    @Override
    public Leader build(MABBuilder b, FieldCollection key, String value) {
        // value is 25 characters (MAB indicator is at first position)
        String s = value.substring(1);
        char satztyp;
        if (s.length() == 24) {
            satztyp = s.charAt(23);
            b.context().resource().add(XBIB_TYPE_RECORD, String.valueOf(satztyp));
            if (satztyp == 'u') {
                b.context().resource().add(BOOST, "0.5");
            }
        } else {
            // invalid leader
        }
        return this;
    }
}
