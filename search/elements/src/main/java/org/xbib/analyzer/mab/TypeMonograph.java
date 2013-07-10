package org.xbib.analyzer.mab;

import org.xbib.elements.marc.extensions.mab.MABElement;

public class TypeMonograph extends MABElement {
    
    private final static MABElement element = new TypeMonograph();
    
    private TypeMonograph() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

}
