package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class RecordLocalOwner extends MABElement {

    private final static MABElement element = new RecordLocalOwner();

    private RecordLocalOwner() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
