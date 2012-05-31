package org.xbib.rdf.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testng.annotations.Test;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.xml.NamespaceContext;
import org.xbib.xml.SimpleNamespaceContext;
import org.xml.sax.InputSource;

public class XmlReaderTest {

    private static final Logger logger = Logger.getLogger(XmlReaderTest.class.getName());

    @Test
    public void testGenericXmlReader() throws Exception {
        String filename = "/test/xml/generic/oro-eprint-25656.xml";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }
        Resource resource = new SimpleResource();
        NamespaceContext ignore = SimpleNamespaceContext.newInstance();
        ignore.addNamespace("oaidc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        XmlReader reader = new XmlReader().setListener(new ResourceBuilder(resource)).setIgnoreNamespaces(ignore).setIdentifier(URI.create("test:id")).parse(new InputSource(in));
    }

    class ResourceBuilder implements StatementListener {

        Resource resource;

        ResourceBuilder(Resource resource) {
            this.resource = resource;
        }

        @Override
        public void newIdentifier(URI uri) {
            resource.setIdentifier(uri);
        }

        @Override
        public void statement(Statement statement) {
            logger.log(Level.INFO, statement.toString());
            resource.add(statement);
        }
    }
}
