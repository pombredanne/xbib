package org.xbib.analyzer.elements.marc;

import org.xbib.analyzer.marc.MARCBuilder;
import org.xbib.analyzer.marc.MARCElement;
import org.xbib.marc.FieldCollection;

public class Skip extends MARCElement {
    
    private final static MARCElement element = new Skip();
    
    private Skip() {
    }
        
    public static MARCElement getInstance() {
        return element;
    }

    @Override
    public void build(MARCBuilder b, FieldCollection key, String value) {
    }

}
