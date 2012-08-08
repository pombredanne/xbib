package org.xbib.analyzer.marc;

import org.xbib.analyzer.marc.addons.MABBuilder;
import org.xbib.analyzer.marc.addons.MABElement;
import org.xbib.marc.FieldList;

public class Error extends MABElement {
    
    private final static MABElement element = new Error();
    
    private Error() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public void build(MABBuilder b, FieldList key, String value) {
    }

}
