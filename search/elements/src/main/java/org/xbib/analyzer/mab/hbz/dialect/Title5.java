package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class Title5 extends MABElement {
    
    private final static MABElement element = new Title5();
    
    private Title5() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
