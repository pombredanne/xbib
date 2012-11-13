package org.xbib.elements.mab;

import org.xbib.marc.FieldCollection;

public class Error extends MABElement {
    
    private final static MABElement element = new Error();
    
    private Error() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public void build(MABBuilder b, FieldCollection key, String value) {
    }

}
