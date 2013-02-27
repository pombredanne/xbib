package org.xbib.rdf.context;

import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleResource;

import java.util.Map;

public class JsonLdContext
        extends AbstractResourceContext<Resource>
        implements ResourceContext<Resource> {

    public static final String CONTEXT = "@context";

    public static final String ID = "@id";

    public static final String TYPE = "@type";

    public static final String LANGUAGE = "@language";

    public static final String CONTAINER = "@container";

    private Map<String,Resource> ids;
    private Map<String,Resource> types;
    private Map<String,Resource> languages;
    private Map<String,Resource> containers;

    @Override
    public JsonLdContext id(IRI identifier) {
        super.id(identifier);
        return this;
    }

    @Override
    public Resource newResource() {
        Resource<Resource,Property,Literal> root = new SimpleResource().id(new IRI().schemeSpecificPart("@context").build());
        Resource resource = root.newResource(CONTEXT);
        for (Map.Entry<String,String> me : namespaceContext().getNamespaces().entrySet()) {
            resource.add(me.getKey(), me.getValue());
        }
        return root;
    }

    public JsonLdContext id(String name, String id) {
        return this;
    }
    public JsonLdContext type(String name, String type) {
        return this;
    }
    public JsonLdContext language(String name, String language) {
        return this;
    }
    public JsonLdContext container(String name, String container) {
        return this;
    }

}
