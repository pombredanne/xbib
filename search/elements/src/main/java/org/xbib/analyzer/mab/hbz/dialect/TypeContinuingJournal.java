package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TypeContinuingJournal extends MABElement {
    
    private final static MABElement element = new TypeContinuingJournal();
    
    private TypeContinuingJournal() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
