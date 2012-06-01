package org.xbib.oai;

import org.xbib.rdf.io.RdfXmlReader;
import org.xbib.rdf.io.XmlReader;
import org.xbib.rdf.io.XmlTriplifier;
import org.xbib.xml.NamespaceContext;
import org.xbib.xml.SimpleNamespaceContext;

public class MetadataPrefixService {

    public static XmlTriplifier getTriplifier(final String metadataPrefix) {
        switch (metadataPrefix.toLowerCase()) {
            case "rdfxml":
                return new RdfXmlReader();
            default: {
                NamespaceContext ignore = SimpleNamespaceContext.newInstance();
                ignore.addNamespace("oaidc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
                return new XmlReader().setIgnoreNamespaces(ignore);
            }
        }
    }
}
