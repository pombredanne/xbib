package org.xbib.json;

import javax.xml.namespace.QName;

public final class JsonXmlReaderFactory {
    
    public static JsonXmlReader createJsonXmlReader(QName root) {
        return new JsonXmlReader(root);
    }
}
