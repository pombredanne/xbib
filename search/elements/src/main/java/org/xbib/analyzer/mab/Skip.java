package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABBuilder;
import org.xbib.elements.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;

public class Skip extends MABElement {
    
    private final static MABElement element = new Skip();
    
    private Skip() {
    }
        
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public Skip build(MABBuilder b, FieldCollection key, String value) {
        return this;
    }
}
