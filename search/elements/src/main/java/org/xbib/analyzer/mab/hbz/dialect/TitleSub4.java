package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleSub4 extends MABElement {
    
    private final static MABElement element = new TitleSub4();
    
    private TitleSub4() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
