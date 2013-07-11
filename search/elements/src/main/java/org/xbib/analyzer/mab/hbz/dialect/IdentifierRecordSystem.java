package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class IdentifierRecordSystem extends MABElement {
    
    private final static MABElement element = new IdentifierRecordSystem();
    
    private IdentifierRecordSystem() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
