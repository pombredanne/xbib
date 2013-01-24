package org.xbib.rdf.jsonld;

import org.xbib.common.Nullable;
import org.xbib.iri.IRI;

public interface JsonLdProcessor {
    
    void expand(String input, JsonLdCallback callback, @Nullable JsonLdOptions options);
    
    void expand(IRI input, JsonLdCallback callback, @Nullable JsonLdOptions options);

    void compact(String input, String context, JsonLdCallback callback, @Nullable JsonLdOptions options);
    
    void compact(IRI input, String context, JsonLdCallback callback, @Nullable JsonLdOptions options);

    void flatten(String input, @Nullable String context, JsonLdCallback callback, @Nullable JsonLdOptions options);
    
    void flatten(IRI input, @Nullable String context, JsonLdCallback callback, @Nullable JsonLdOptions options);
}
