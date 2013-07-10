package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceCreator extends MABElement {
    
    private final static MABElement element = new SourceCreator();
    
    private SourceCreator() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
