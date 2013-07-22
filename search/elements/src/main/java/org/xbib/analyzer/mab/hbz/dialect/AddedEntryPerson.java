package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;

public class AddedEntryPerson extends MABElement {

    private final static MABElement element = new AddedEntryPerson();

    private AddedEntryPerson() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
