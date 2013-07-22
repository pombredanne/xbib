package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class RecordType extends MABElement {

    private final static MABElement element = new RecordType();

    private RecordType() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
