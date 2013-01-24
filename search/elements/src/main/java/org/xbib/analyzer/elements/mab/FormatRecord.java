package org.xbib.analyzer.elements.mab;

import org.xbib.analyzer.marc.extensions.mab.MABBuilder;
import org.xbib.analyzer.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class FormatRecord extends MABElement {
    
    private final static MABElement element = new FormatRecord();
    
    private FormatRecord() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public FormatRecord build(MABBuilder b, FieldCollection key, String value) {
        // b.context().getResource(b.context().resource(), ...).add( ... , value);
        return this;
    }

}
