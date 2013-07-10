package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleUniform3 extends MABElement {
    
    private final static MABElement element = new TitleUniform3();
    
    private TitleUniform3() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
