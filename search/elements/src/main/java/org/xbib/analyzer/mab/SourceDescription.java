package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceDescription extends MABElement {
    
    private final static MABElement element = new SourceDescription();
    
    private SourceDescription() {
    }
    
    public static MABElement getInstance() {
        return element;
    }


}
