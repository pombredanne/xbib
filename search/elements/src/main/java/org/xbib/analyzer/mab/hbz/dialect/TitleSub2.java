package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleSub2 extends MABElement {
    
    private final static MABElement element = new TitleSub2();
    
    private TitleSub2() {
    }
    
    public static MABElement getInstance() {
        return element;
    }


}
