package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleUniform1 extends MABElement {
    
    private final static MABElement element = new TitleUniform1();
    
    private TitleUniform1() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
