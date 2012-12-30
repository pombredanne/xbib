package org.xbib.analyzer.elements.mab;

import org.xbib.analyzer.marc.extensions.mab.MABBuilder;
import org.xbib.analyzer.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class DateIssued extends MABElement {
    
    private final static MABElement element = new DateIssued();
    
    private DateIssued() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public DateIssued build(MABBuilder b, FieldCollection key, String value) {
        // b.context().getResource(b.context().resource(), ...).add( ... , value);
        return this;
    }

}
