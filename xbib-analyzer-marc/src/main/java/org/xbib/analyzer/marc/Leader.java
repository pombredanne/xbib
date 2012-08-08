package org.xbib.analyzer.marc;

import org.xbib.analyzer.marc.addons.MABBuilder;
import org.xbib.analyzer.marc.addons.MABElement;
import org.xbib.marc.FieldList;

public class Leader extends MABElement {

    private final static MABElement element = new Leader();

    private Leader() {
    }

    public static MABElement getInstance() {
        return element;
    }

    @Override
    public void build(MABBuilder b, FieldList key, String value) {
        // value is 25 characters (MAB indicator is at first position)
        String s = value.substring(1);
        char satztyp;
        if (s.length() == 24) {
            satztyp = s.charAt(23);
            b.context().getResource(b.context().resource(), TYPE).addProperty(XBIB_TYPE_RECORD, String.valueOf(satztyp));
            if (satztyp == 'u') {
                b.context().resource().addProperty(BOOST, "0.5");
            }
        } else {
            // invalid leader
        }
    }
}