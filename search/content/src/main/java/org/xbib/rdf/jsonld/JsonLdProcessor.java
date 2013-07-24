package org.xbib.rdf.jsonld;

import org.xbib.iri.IRI;

public interface JsonLdProcessor {
    
    void expand(String input, JsonLdCallback callback, JsonLdOptions options);
    
    void expand(IRI input, JsonLdCallback callback, JsonLdOptions options);

    void compact(String input, String context, JsonLdCallback callback, JsonLdOptions options);
    
    void compact(IRI input, String context, JsonLdCallback callback, JsonLdOptions options);

    void flatten(String input, String context, JsonLdCallback callback, JsonLdOptions options);
    
    void flatten(IRI input, String context, JsonLdCallback callback, JsonLdOptions options);
}
