package org.xbib.rdf.xcontent;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.Loggers;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.simple.SimpleLiteral;
import org.xbib.rdf.simple.SimpleResource;

public class DefaultContentBuilderTest<S extends Identifier, P extends Property, O extends Node>
    extends Assert {

    private final Logger logger = Loggers.getLogger(DefaultContentBuilderTest.class);

    @Test
    public void testSimpleBuilder() throws Exception {
        Resource<S, P, O> resource = new SimpleResource<>();
        SimpleLiteral<String> l = new SimpleLiteral()
                .object("2013")
                .type(IRI.create("xsd:gYear"));
        resource.id(IRI.create("urn:resource"))
                .add("urn:property", "Hello World")
                .add("urn:date", l)
                .add("urn:link", IRI.create("urn:pointer"));
        ResourceContext context = resource.context();
        DefaultContentBuilder defaultContentBuilder = new DefaultContentBuilder();
        String result = defaultContentBuilder.build(context, resource);
        logger.info("simple: {}", result);
    }

    @Test
    public void testRDFType() throws Exception {
        Resource<S, P, O> resource = new SimpleResource<>();
        SimpleLiteral<String> l = new SimpleLiteral()
                .object("2013")
                .type(IRI.create("xsd:gYear"));
        resource.id(IRI.create("urn:resource"))
                .add("urn:property", "Hello World")
                .add("urn:date", l)
                .add("rdf:type", IRI.create("urn:type1"))
                .newResource("urn:embedded")
                .add("rdf:type", IRI.create("urn:type2"));
        ResourceContext context = resource.context();
        DefaultContentBuilder defaultContentBuilder = new DefaultContentBuilder();
        String result = defaultContentBuilder.build(context, resource);
        logger.info("embbeded: {}", result);
    }

}
