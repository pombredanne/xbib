package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class IdentifierIssn extends MABElement {
    
    private final static MABElement element = new IdentifierIssn();
    
    private IdentifierIssn() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
