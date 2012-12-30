package org.xbib.analyzer.elements.mab;

import org.xbib.analyzer.marc.extensions.mab.MABBuilder;
import org.xbib.analyzer.marc.extensions.mab.MABElement;
import org.xbib.analyzer.marc.extensions.mab.MABValueMapper;
import org.xbib.marc.FieldCollection;

import java.util.Map;

public class FormatCarrier extends MABElement {

    private final static MABElement element = new FormatCarrier();
    private static Map<String,String> format = new MABValueMapper("format").getMap();
    private static Map<String,String> carriers = new MABValueMapper("carriers").getMap();

    private FormatCarrier() {
    }

    public static MABElement getInstance() {
        return element;
    }

    public Map getFormats() {
        return format;
    }

    @Override
    public FormatCarrier build(MABBuilder b, FieldCollection key, String value) {
        b.context().getResource(b.context().resource(), FORMAT).add(DCTERMS_MEDIUM, value);
        return this;
    }
    
}
