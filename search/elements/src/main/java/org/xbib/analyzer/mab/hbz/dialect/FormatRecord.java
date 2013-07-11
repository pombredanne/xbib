package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class FormatRecord extends MABElement {
    
    private final static MABElement element = new FormatRecord();
    
    private FormatRecord() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
