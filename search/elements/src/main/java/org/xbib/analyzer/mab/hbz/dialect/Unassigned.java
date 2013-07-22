package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class Unassigned extends MABElement {

    private final static MABElement element = new Unassigned();

    private Unassigned() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
