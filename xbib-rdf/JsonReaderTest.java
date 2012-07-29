package org.xbib.rdf.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.testng.annotations.Test;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.xml.SimpleNamespaceContext;
import org.xml.sax.InputSource;

public class JsonReaderTest {

    private static final Logger logger = Logger.getLogger(JsonReaderTest.class.getName());

    @Test
    public void testJsonReader() throws Exception {
        String filename = "/test/json/dc.json";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }
        Resource resource = new SimpleResource();
        //QName root = new QName("http://xbib.org/elements/", "root", "xbib");
        SimpleNamespaceContext context = SimpleNamespaceContext.getInstance();
        context.addNamespace("lia", "http://xbib.org/lia/");
        JsonReader reader = new JsonReader().setListener(new ResourceBuilder(resource))
                .setIdentifier(URI.create("test:id")).parse(new InputSource(in));
        logger.log(Level.INFO, resource.toString());
        
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
            resource.add(statement);
        }
    }
}
