package org.xbib.rdf.io.json;

import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.RDF;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.rdf.xcontent.Builder;

import java.io.IOException;
import java.util.Map;
import java.util.Stack;

import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

public class JsonWriter<S extends Identifier, P extends Property, O extends Node>
        implements TripleListener<S,P,O> {

    private final Logger logger = LoggerFactory.getLogger(JsonWriter.class.getName());

    private IRINamespaceContext context;

    private Resource resource;

    private String translatePicaSortMarker;

    private boolean nsWritten;

    private Builder builder;

    private StringBuilder sb;

    private long byteCounter;

    private long idCounter;

    public JsonWriter() {
        this.context = IRINamespaceContext.newInstance();
        this.nsWritten = false;
        this.resource = new SimpleResource();
        this.builder = new Builder();
        this.sb = new StringBuilder();
        this.translatePicaSortMarker = null;
    }

    public JsonWriter translatePicaSortMarker(String marker) {
        this.translatePicaSortMarker = marker;
        return this;
    }

    @Override
    public JsonWriter newIdentifier(IRI iri) {
        if (!iri.equals(resource.id())) {
            try {
                if (!nsWritten) {
                    writeNamespaces();
                }
                builder.build(resource.context(), resource);
                idCounter++;
                resource = new SimpleResource();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        resource.id(iri);
        return this;
    }

    @Override
    public TripleListener<S, P, O> startPrefixMapping(String prefix, String uri) {
        return null;
    }

    @Override
    public TripleListener<S, P, O> endPrefixMapping(String prefix) {
        return null;
    }


    @Override
    public TripleListener<S, P, O> triple(Triple<S, P, O> triple) {
        return null;
    }

    public JsonWriter writeNamespaces() throws IOException {
        if (context == null) {
            return this;
        }
        nsWritten = false;
        for (Map.Entry<String, String> entry : context.getNamespaces().entrySet()) {
            if (entry.getValue().length() > 0) {
                String nsURI = entry.getValue().toString();
                if (!RDF.NS_URI.equals(nsURI)) {
                    writeNamespace(entry.getKey(), nsURI);
                    nsWritten = true;
                }
            }
        }
        return this;
    }

    private void writeNamespace(String prefix, String name) throws IOException {

    }

}
