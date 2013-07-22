package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.ElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElement;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.rdf.Resource;

public class ConferenceAlternative extends MABElement {
    
    private final static MABElement element = new ConferenceAlternative();
    
    private ConferenceAlternative() {
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
                        String s = field.data();
                        if (!skip(s)) {
                            person.add("conferenceAlternativeName", clean(s));
                        }
                        break;
                    }
                    case "b" : {
                        person.add("conferenceAlternativeUnit", field.data());
                        break;
                    }
                    case "h" : {
                        person.add("conferenceAlternativeOtherInformation", field.data());
                        break;
                    }
                    case "e" : {
                        String s = field.data();
                        if (!skip(s)) {
                            person.add("conferenceAlternativeName", clean(s));
                        }
                        break;
                    }
                    case "n" : {
                        person.add("conferenceAlternativeNumbering", field.data());
                        break;
                    }
                }
            }
            return true; // done!
        }
    }

    private String clean(String value) {
        return value.replaceAll("\\s*<<.*?>>", "")
                .replaceAll("\\s*<.*?>", "");
    }

    private boolean skip(String value) {
        return value.indexOf("...") > 0 || value.indexOf("***") > 0;
    }

}
