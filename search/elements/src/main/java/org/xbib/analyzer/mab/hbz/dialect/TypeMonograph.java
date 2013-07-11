package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TypeMonograph extends MABElement {
    
    private final static MABElement element = new TypeMonograph();
    
    private TypeMonograph() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
