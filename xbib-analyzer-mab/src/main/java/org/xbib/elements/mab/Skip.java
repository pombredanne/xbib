package org.xbib.elements.mab;

import org.xbib.marc.FieldCollection;


public class Skip extends MABElement {
    
    private final static MABElement element = new Skip();
    
    private Skip() {
    }
        
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public void build(MABBuilder b, FieldCollection key, String value) {
    }
}
