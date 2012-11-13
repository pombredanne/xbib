package org.xbib.elements.mab;

import org.xbib.marc.FieldDesignatorList;

public class Skip extends MABElement {
    
    private final static MABElement element = new Skip();
    
    private Skip() {
    }
        
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public void build(MABBuilder b, FieldDesignatorList key, String value) {
    }
}
