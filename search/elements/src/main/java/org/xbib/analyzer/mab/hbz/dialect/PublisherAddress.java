package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class PublisherAddress extends MABElement {

    private final static MABElement element = new PublisherAddress();

    private PublisherAddress() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
