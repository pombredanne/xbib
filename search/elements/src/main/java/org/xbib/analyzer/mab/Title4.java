package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class Title4 extends MABElement {
    
    private final static MABElement element = new Title4();
    
    private Title4() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
