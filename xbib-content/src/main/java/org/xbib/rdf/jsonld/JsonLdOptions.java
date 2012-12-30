package org.xbib.rdf.jsonld;

import org.xbib.iri.IRI;

public class JsonLdOptions {

    IRI base;
    JsonLdContext expandContext = null;
    boolean compactArrays = true;
    boolean optimize = false;
    boolean useRdfType = false;
    boolean useNativeTypes = true;
}
