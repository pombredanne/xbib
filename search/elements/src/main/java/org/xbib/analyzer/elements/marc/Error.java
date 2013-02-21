package org.xbib.analyzer.elements.marc;

import org.xbib.elements.marc.MARCBuilder;
import org.xbib.elements.marc.MARCElement;
import org.xbib.marc.FieldCollection;

public class Error extends MARCElement {
    
    private final static MARCElement element = new Error();
    
    private Error() {
    }
    
    public static MARCElement getInstance() {
        return element;
    }

    @Override
    public Error build(MARCBuilder b, FieldCollection key, String value) {
        return this;
    }

}
