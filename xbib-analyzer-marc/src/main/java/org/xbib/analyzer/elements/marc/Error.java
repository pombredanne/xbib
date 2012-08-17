package org.xbib.analyzer.elements.marc;

import org.xbib.analyzer.marc.MARCBuilder;
import org.xbib.analyzer.marc.MARCElement;
import org.xbib.marc.FieldCollection;

public class Error extends MARCElement {
    
    private final static MARCElement element = new Error();
    
    private Error() {
    }
    
    public static MARCElement getInstance() {
        return element;
    }

    @Override
    public void build(MARCBuilder b, FieldCollection key, String value) {
    }

}
