package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.ElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElement;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.rdf.Resource;

public class CorporateBody extends MABElement {

    private final static MABElement element = new CorporateBody();

    private CorporateBody() {
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
                        person.add("corporateBodyName", field.data());
                        break;
                    }
                    case "b" : {
                        person.add("corporateBodyUnit", field.data());
                        break;
                    }
                    case "h" : {
                        person.add("corporateBodyOtherInformation", field.data());
                        break;
                    }
                    case "k" : {
                        person.add("corporateBodyName", field.data());
                        break;
                    }
                    case "n" : {
                        person.add("corporateBodyNumbering", field.data());
                        break;
                    }
                    case "9" : {
                        person.add("corporateBodyIdentifier", field.data());
                        break;
                    }
                }
            }
            return true; // done!
        }
    }
}
