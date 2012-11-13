package org.xbib.elements.mab;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.xbib.elements.ElementMapFactory;
import org.xbib.marc.FieldCollection;

public class FormatCarrier extends MABElement {

    private final static MABElement element = new FormatCarrier();
    private static Map format = null;
    private static Map carriers = null;

    static {
        try {
            InputStream json = ElementMapFactory.class.getResourceAsStream("/org/xbib/elements/mab/format.json");
            if (json == null) {
                throw new IOException("format.json not found");
            }
            format = new ObjectMapper().readValue(json, HashMap.class);
            json = ElementMapFactory.class.getResourceAsStream("/org/xbib/elements/mab/carriers.json");
            if (json == null) {
                throw new IOException("carriers.json not found");
            }
            carriers = new ObjectMapper().readValue(json, HashMap.class);
        } catch (Exception ex) {
            logger.info(null, ex);
        }
    }

    private FormatCarrier() {
    }

    public static MABElement getInstance() {
        return element;
    }

    public Map getFormats() {
        return format;
    }

    @Override
    public void build(MABBuilder b, FieldCollection key, String value) {
        b.context().getResource(b.context().resource(), FORMAT).addProperty(DCTERMS_MEDIUM, value);
    }
    
}
