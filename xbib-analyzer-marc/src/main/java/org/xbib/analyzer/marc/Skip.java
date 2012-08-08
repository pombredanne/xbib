package org.xbib.analyzer.marc;

import org.xbib.analyzer.marc.addons.MABBuilder;
import org.xbib.analyzer.marc.addons.MABElement;
import org.xbib.marc.FieldList;

public class Skip extends MABElement {
    
    private final static MABElement element = new Skip();
    
    private Skip() {
    }
        
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public void build(MABBuilder b, FieldList key, String value) {
    }
}
