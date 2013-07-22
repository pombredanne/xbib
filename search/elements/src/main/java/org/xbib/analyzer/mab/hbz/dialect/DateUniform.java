package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class DateUniform extends MABElement {

    private final static MABElement element = new DateUniform();

    private DateUniform() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
