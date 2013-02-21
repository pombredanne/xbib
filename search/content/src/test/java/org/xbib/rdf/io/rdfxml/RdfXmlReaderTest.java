package org.xbib.rdf.io.rdfxml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xml.sax.InputSource;

public class RdfXmlReaderTest {

    @Test
    public void testReader() throws Exception {
        String filename = "/org/xbib/rdf/io/118540238.xml";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }
        Resource root = new SimpleResource();
        TripleListener listener = new ResourceBuilder(root);
        RdfXmlReader reader = new RdfXmlReader();
        reader.setListener(listener);
        reader.parse(new InputSource(in));
        //logger.log(Level.INFO, root.toString());
        StringWriter sw = new StringWriter();
        IRINamespaceContext context = IRINamespaceContext.getInstance();
        context.addNamespace("gnd", "http://d-nb.info/gnd/");
        context.addNamespace("rdagr2", "http://RDVocab.info/ElementsGr2/");
        context.addNamespace("foaf", "http://xmlns.com/foaf/0.1/");
        context.addNamespace("rel", "http://purl.org/vocab/relationship/");
        context.addNamespace("marclang", "http://marccodes.heroku.com/languages/");
        context.addNamespace("owl", "http://www.w3.org/2002/07/owl#");
        TurtleWriter t = new TurtleWriter(context);
        t.write(root, true, sw);
        String s2 = sw.toString().trim();
        //logger.log(Level.INFO, "turtle = {0}", s2);
    }

    class ResourceBuilder implements TripleListener {

        Resource resource;

        ResourceBuilder(Resource resource) {
            this.resource = resource;
        }
        
        @Override
        public ResourceBuilder newIdentifier(IRI uri) {
            resource.id(uri);
            return this;
        }

        @Override
        public ResourceBuilder triple(Triple triple) {
            //logger.log(Level.INFO, triple.toString());
            resource.add(triple);
            return this;
        }
    }
}
