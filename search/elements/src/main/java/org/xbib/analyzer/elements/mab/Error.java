package org.xbib.analyzer.elements.mab;

import org.xbib.elements.marc.extensions.mab.MABBuilder;
import org.xbib.elements.marc.extensions.mab.MABElement;
import org.xbib.marc.FieldCollection;

public class Error extends MABElement {
    
    private final static MABElement element = new Error();
    
    private Error() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public Error build(MABBuilder b, FieldCollection key, String value) {
        return this;
    }

}
