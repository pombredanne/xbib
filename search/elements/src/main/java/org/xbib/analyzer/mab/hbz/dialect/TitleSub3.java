package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TitleSub3 extends MABElement {
    
    private final static MABElement element = new TitleSub3();
    
    private TitleSub3() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
