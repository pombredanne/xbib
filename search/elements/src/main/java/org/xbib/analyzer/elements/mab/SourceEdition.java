package org.xbib.analyzer.elements.mab;

import org.xbib.elements.marc.extensions.mab.MABBuilder;
import org.xbib.elements.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;


public class SourceEdition extends MABElement {
    
    private final static MABElement element = new SourceEdition();
    
    private SourceEdition() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public SourceEdition build(MABBuilder b, FieldCollection key, String value) {
        // b.context().getResource(b.context().resource(), ...).add( ... , value);
        return this;
    }

}
