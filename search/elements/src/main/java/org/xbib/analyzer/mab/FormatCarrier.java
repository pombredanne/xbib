package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class FormatCarrier extends MABElement {

    private final static MABElement element = new FormatCarrier();

    private FormatCarrier() {
    }

    public static MABElement getInstance() {
        return element;
    }

}
