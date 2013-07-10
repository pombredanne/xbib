package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class Source extends MABElement {
    
    private final static MABElement element = new Source();
    
    private Source() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
