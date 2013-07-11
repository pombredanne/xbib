package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceCreator extends MABElement {
    
    private final static MABElement element = new SourceCreator();
    
    private SourceCreator() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
