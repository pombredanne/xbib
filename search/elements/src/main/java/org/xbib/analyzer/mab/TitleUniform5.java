package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleUniform5 extends MABElement {
    
    private final static MABElement element = new TitleUniform5();
    
    private TitleUniform5() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
