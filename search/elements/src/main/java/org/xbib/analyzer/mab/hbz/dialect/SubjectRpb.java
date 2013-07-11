package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SubjectRpb extends MABElement {
    
    private final static MABElement element = new SubjectRpb();
    
    private SubjectRpb() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
