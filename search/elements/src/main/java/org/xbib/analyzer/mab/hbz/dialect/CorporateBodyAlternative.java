package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.ElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElement;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.rdf.Resource;

public class CorporateBodyAlternative extends MABElement {
    
    private final static MABElement element = new CorporateBodyAlternative();
    
    private CorporateBodyAlternative() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    /**
     * a = Name (alt)
     * k = Name Körperschaft (neu)
     * 9 = GND-ID (neu) / Norm-ID (alt)
     * b = untergeordnete Körperschaft
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
                            person.add("corporateBodyAlternativeName", clean(s));
                        }
                        break;
                    }
                    case "b" : {
                        person.add("corporateBodyAlternativeUnit", field.data());
                        break;
                    }
                    case "h" : {
                        person.add("corporateBodyAlternativeOtherInformation", field.data());
                        break;
                    }
                    case "k" : {
                        String s = field.data();
                        if (!skip(s)) {
                            person.add("corporateBodyAlternativeName", clean(s));
                        }
                        break;
                    }
                    case "n" : {
                        person.add("corporateBodyAlternativeNumbering", field.data());
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
