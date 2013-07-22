package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class SubjectDopaed extends MABElement {
    
    private final static MABElement element = new SubjectDopaed();
    
    private SubjectDopaed() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
