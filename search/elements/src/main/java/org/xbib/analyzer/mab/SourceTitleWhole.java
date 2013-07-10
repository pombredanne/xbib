package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceTitleWhole extends MABElement {
    
    private final static MABElement element = new SourceTitleWhole();
    
    private SourceTitleWhole() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
