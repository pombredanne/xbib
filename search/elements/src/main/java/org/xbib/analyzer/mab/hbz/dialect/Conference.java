package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.ElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElement;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.rdf.Resource;

public class Conference extends MABElement {

    private final static MABElement element = new Conference();

    private Conference() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    /**
     * e = Name Konferenz
     * 9 = GND-ID (neu) / Norm-ID (alt)
     * b = untergeordnete Einheit
     * c = Ort
     * d = Datum
     * h = Zusatz
     * n = Zählung
     *
     * @param builder
     * @param fields
     * @param value
     * @return
     */

    @Override
    public boolean fields(ElementBuilder<FieldCollection, String, MABElement, MABContext> builder,
                          FieldCollection fields, String value) {
        if (value != null) {
            return super.fields(builder, fields, value);
        } else {
            Resource person = builder.context().resource().newResource(DC_CONTRIBUTOR);
            for (Field field : fields) {
                switch (field.subfieldId()) {
                    case "a" : {
                        person.add("conferenceName", field.data());
                        break;
                    }
                    case "b" : {
                        person.add("conferenceUnit", field.data());
                        break;
                    }
                    case "c" : {
                        person.add("conferencePlace", field.data());
                        break;
                    }
                    case "d" : {
                        person.add("conferenceDate", field.data());
                        break;
                    }
                    case "h" : {
                        person.add("conferenceOtherInformation", field.data());
                        break;
                    }
                    case "e" : {
                        person.add("conferenceName", field.data());
                        break;
                    }
                    case "n" : {
                        person.add("conferenceNumbering", field.data());
                        break;
                    }
                    case "9" : {
                        person.add("conferenceIdentifier", field.data());
                        break;
                    }
                }
            }
            return true; // done!
        }
    }
}
