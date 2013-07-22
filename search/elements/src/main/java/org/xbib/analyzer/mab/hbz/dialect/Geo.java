package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.ElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElement;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.rdf.Resource;

public class Geo extends MABElement {
    
    private final static MABElement element = new Geo();
    
    private Geo() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
    /**
     * g = Geografikum (Gebietskörperschaft) (NW)
     * 9 = GND-Identifikationsnummer
     * h = Zusatz (W)
     * x = nachgeordneter Teil (W)
     * z = geografische Unterteilung (W)
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
                    case "g" : {
                        person.add("geoName", field.data());
                        break;
                    }
                    case "h" : {
                        person.add("geoOtherInformation", field.data());
                        break;
                    }
                    case "x" : {
                        person.add("geoSubUnit", field.data());
                        break;
                    }
                    case "z" : {
                        person.add("geoZone", field.data());
                        break;
                    }
                    case "9" : {
                        person.add("geoIdentifier", field.data());
                        break;
                    }
                }
            }
            return true; // done!
        }
    }
}
