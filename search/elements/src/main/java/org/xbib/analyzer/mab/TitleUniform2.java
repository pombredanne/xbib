package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleUniform2 extends MABElement {
    
    private final static MABElement element = new TitleUniform2();
    
    private TitleUniform2() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
