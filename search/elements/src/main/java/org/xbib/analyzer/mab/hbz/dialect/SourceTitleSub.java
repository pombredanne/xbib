package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceTitleSub extends MABElement {
    
    private final static MABElement element = new SourceTitleSub();
    
    private SourceTitleSub() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
