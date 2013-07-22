package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class IdentifierCasalini extends MABElement {

    private final static MABElement element = new IdentifierCasalini();

    private IdentifierCasalini() {
    }
    
    public static MABElement getInstance() {
        return element;
    }
}
