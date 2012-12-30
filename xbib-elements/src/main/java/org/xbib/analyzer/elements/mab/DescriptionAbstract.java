package org.xbib.analyzer.elements.mab;

import org.xbib.analyzer.marc.extensions.mab.MABBuilder;
import org.xbib.analyzer.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class DescriptionAbstract extends MABElement {
    
    private final static MABElement element = new DescriptionAbstract();
    
    private DescriptionAbstract() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public DescriptionAbstract build(MABBuilder b, FieldCollection key, String value) {
        // b.context().getResource(b.context().resource(), ...).add( ... , value);
        return this;
    }

}
