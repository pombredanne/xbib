package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceTitleSub extends MABElement {
    
    private final static MABElement element = new SourceTitleSub();
    
    private SourceTitleSub() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
