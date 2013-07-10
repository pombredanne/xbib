package org.xbib.analyzer.mab;

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
        super.build(b, key, value);
        // value is 25 characters! (MAB indicator is at first position)
        String s = value.substring(1);
        if (s.length() == 24) {
            char satztyp = s.charAt(23);
            b.context().resource().add(XBIB_TYPE_RECORD, String.valueOf(satztyp));
            if (satztyp == 'u') {
                b.context().resource().add(BOOST, "0.5");
            }
        }
        return this;
    }
}
