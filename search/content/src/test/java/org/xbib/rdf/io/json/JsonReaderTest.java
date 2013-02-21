package org.xbib.rdf.io.json;

import java.io.IOException;
import java.io.InputStream;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import javax.xml.namespace.QName;

public class JsonReaderTest extends Assert {

    private static final Logger logger = LoggerFactory.getLogger(JsonReaderTest.class.getName());

    final SimpleResourceContext resourceContext = new SimpleResourceContext();

    @Test
    public void testGenericJsonReader() throws Exception {

        String filename = "dc.json";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }

        IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        context.addNamespace("dcterms", "http://purl.org/dc/terms/");
        context.addNamespace("bib", "info:srw/cql-context-set/1/bib-v1/");
        context.addNamespace("xbib", "http://xbib.org/");
        context.addNamespace("lia", "http://xbib.org/lia/");

        resourceContext.newNamespaceContext(context);

        JsonResourceHandler jsonHandler = new JsonResourceHandler(resourceContext) {

            @Override
            public boolean isResourceDelimiter(QName name) {
                //return "oai_dc".equals(name.getLocalPart());
                return false;
            }

            @Override
            public boolean skip(QName name) {
                // skip dc:dc element
                //return "dc".equals(name.getLocalPart());
                return false;
            }

            @Override
            public void identify(QName name, String value, IRI identifier) {
                if (identifier == null) {
                    // make sure we can build an opaque IRI, whatever is out there
                   // String s = UrlEncoding.encode(value, CharUtils.Profile.SCHEMESPECIFICPART.filter());
                    resourceContext.resource().id(IRI.create("id:doc1"));
                }
            }

        };
        jsonHandler.setListener(new ResourceBuilder());
        new JsonReader()
                .setHandler(jsonHandler)
                .root(new QName("http://purl.org/dc/elements/1.1/", "root", "dc"))
                .parse(in);
        /*StringWriter sw = new StringWriter();
        TurtleWriter t = new TurtleWriter();
        t.write(resourceContext.resource(), true, sw);
        logger.info("resource={}", sw.toString());
        */
    }

    class ResourceBuilder implements TripleListener {

        @Override
        public ResourceBuilder newIdentifier(IRI iri) {
            resourceContext.resource().id(iri);
            return this;
        }

        @Override
        public ResourceBuilder triple(Triple triple) {
            logger.info("stmt={}", triple);
            resourceContext.resource().add(triple);
            return this;
        }
    }

}
