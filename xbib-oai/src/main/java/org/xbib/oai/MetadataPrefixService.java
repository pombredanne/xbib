package org.xbib.oai;

import org.xbib.rdf.io.RdfXmlReader;
import org.xbib.rdf.io.XmlReader;
import org.xbib.rdf.io.XmlTriplifier;

public class MetadataPrefixService {

    public static XmlTriplifier getTriplifier(final String metadataPrefix) {
        switch (metadataPrefix.toLowerCase()) {
            case "rdfxml":
                return new RdfXmlReader();
            default: {
                return new XmlReader();
            }
        }
    }
}
