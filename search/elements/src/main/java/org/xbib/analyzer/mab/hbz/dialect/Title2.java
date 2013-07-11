package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class Title2 extends MABElement {
    
    private final static MABElement element = new Title2();
    
    private Title2() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
