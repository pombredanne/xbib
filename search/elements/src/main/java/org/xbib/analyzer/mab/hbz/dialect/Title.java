package org.xbib.analyzer.mab.hbz.dialect;

import org.xbib.elements.marc.dialects.mab.MABElement;
import org.xbib.rdf.Resource;

public class Title extends MABElement {
    
    private final static MABElement element = new Title();
    
    Title() {
    }
    
    public static MABElement getInstance() {
        return element;
    }

    @Override
    public String data(String predicate, Resource resource, String property, String value) {
        return value
                .replace('\u0098', '\u00ac')
                .replace('\u009c', '\u00ac')
                .replaceAll("<<(.*?)>>", "¬$1¬")
                .replaceAll("<(.*?)>", "[$1]");
    }

}
