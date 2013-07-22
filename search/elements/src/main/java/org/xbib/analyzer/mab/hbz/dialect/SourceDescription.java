package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SourceDescription extends MABElement {
    
    private final static MABElement element = new SourceDescription();
    
    private SourceDescription() {
    }
    
    public static MABElement getInstance() {
        return element;
    }


}
