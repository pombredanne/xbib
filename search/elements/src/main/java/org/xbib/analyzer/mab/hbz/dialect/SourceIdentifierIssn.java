package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceIdentifierIssn extends MABElement {
    
    private final static MABElement element = new SourceIdentifierIssn();
    
    private SourceIdentifierIssn() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
