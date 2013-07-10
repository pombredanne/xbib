package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceEdition extends MABElement {
    
    private final static MABElement element = new SourceEdition();
    
    private SourceEdition() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
