package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class Title3 extends MABElement {
    
    private final static MABElement element = new Title3();
    
    private Title3() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
