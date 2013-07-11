package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleRelated3 extends MABElement {
    
    private final static MABElement element = new TitleRelated3();
    
    private TitleRelated3() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
