package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class LanguageSequence extends MABElement {
    
    private final static MABElement element = new LanguageSequence();
    
    private LanguageSequence() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
