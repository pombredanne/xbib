package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleUniform extends MABElement {
    
    private final static MABElement element = new TitleUniform();
    
    private TitleUniform() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
