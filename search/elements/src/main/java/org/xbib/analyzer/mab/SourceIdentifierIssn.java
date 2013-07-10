package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class SourceIdentifierIssn extends MABElement {
    
    private final static MABElement element = new SourceIdentifierIssn();
    
    private SourceIdentifierIssn() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
