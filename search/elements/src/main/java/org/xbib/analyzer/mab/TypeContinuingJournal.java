package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TypeContinuingJournal extends MABElement {
    
    private final static MABElement element = new TypeContinuingJournal();
    
    private TypeContinuingJournal() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
