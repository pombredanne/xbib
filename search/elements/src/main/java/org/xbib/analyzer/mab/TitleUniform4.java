package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleUniform4 extends MABElement {
    
    private final static MABElement element = new TitleUniform4();
    
    private TitleUniform4() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
