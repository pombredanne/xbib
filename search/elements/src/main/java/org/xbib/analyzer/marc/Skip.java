package org.xbib.analyzer.marc;

import org.xbib.elements.marc.MARCElementBuilder;
import org.xbib.elements.marc.MARCElement;
import org.xbib.marc.FieldCollection;

public class Skip extends MARCElement {
    
    private final static MARCElement element = new Skip();
    
    private Skip() {
    }
        
    public static MARCElement getInstance() {
        return element;
    }

    @Override
    public Skip build(MARCElementBuilder b, FieldCollection key, String value) {
        return this;
    }

}
