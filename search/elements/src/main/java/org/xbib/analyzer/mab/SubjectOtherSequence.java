package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectOtherSequence extends MABElement {
    
    private final static MABElement element = new SubjectOtherSequence();
    
    private SubjectOtherSequence() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
