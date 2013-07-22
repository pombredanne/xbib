package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.ElementBuilder;
import org.xbib.elements.marc.dialects.mab.MABContext;
import org.xbib.elements.marc.dialects.mab.MABElement;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.rdf.Resource;

public class Person extends MABElement {

    private final static MABElement element = new Person();

    Person() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

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
                        person.add("personName", field.data());
                        break;
                    }
                    case "b" : {
                        person.add("personRole", field.data());
                        break;
                    }
                    case "c" : {
                        person.add("personTitle", field.data());
                        break;
                    }
                    case "d" : {
                        person.add("personBio", field.data());
                        break;
                    }
                    case "p" : {
                        person.add("personName", field.data());
                        break;
                    }
                    case "n" : {
                        person.add("personNumbering", field.data());
                        break;
                    }
                    case "9" : {
                        person.add("personIdentifier", field.data());
                        break;
                    }
                }
            }
            return true; // done!
        }
    }
}
