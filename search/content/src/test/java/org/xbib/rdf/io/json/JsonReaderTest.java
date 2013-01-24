package org.xbib.rdf.io.json;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;
import org.xbib.rdf.io.StatementListener;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.xml.XMLNamespaceContext;
import org.xml.sax.InputSource;

public class JsonReaderTest {

    private static final Logger logger = Logger.getLogger(JsonReaderTest.class.getName());

    @Test
    public void testJsonReader() throws Exception {
        InputStream in = getClass().getResourceAsStream("dc.json");
        Resource resource = new SimpleResource();
        XMLNamespaceContext context = XMLNamespaceContext.getInstance();
        context.addNamespace("lia", "http://xbib.org/lia/");
        JsonReader reader = new JsonReader().setListener(new ResourceBuilder(resource))
                .setIdentifier(new IRI().curi("test:id")).parse(new InputSource(in));
        logger.log(Level.INFO, resource.toString());
    }

    class ResourceBuilder implements StatementListener {

        Resource resource;

        ResourceBuilder(Resource resource) {
            this.resource = resource;
        }

        @Override
        public void newIdentifier(IRI uri) {
            resource.id(uri);
        }

        @Override
        public void statement(Statement statement) {
            resource.add(statement);
        }
    }
}
