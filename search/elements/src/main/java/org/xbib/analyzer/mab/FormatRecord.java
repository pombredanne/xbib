package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class FormatRecord extends MABElement {
    
    private final static MABElement element = new FormatRecord();
    
    private FormatRecord() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
