package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SourceIdentifierIsbn extends MABElement {
    
    private final static MABElement element = new SourceIdentifierIsbn();
    
    private SourceIdentifierIsbn() {
    }
    
    public static MABElement getInstance() {
        return element;
    }


}
