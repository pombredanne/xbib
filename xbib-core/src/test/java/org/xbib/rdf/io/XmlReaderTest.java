package org.xbib.rdf.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.testng.annotations.Test;
import org.xbib.rdf.Statement;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.xml.NamespaceContext;
import org.xbib.xml.SimpleNamespaceContext;
import org.xml.sax.InputSource;

public class XmlReaderTest {

    private static final Logger logger = Logger.getLogger(XmlReaderTest.class.getName());
    private final SimpleResourceContext src = new SimpleResourceContext();

    @Test
    public void testGenericXmlReader() throws Exception {
        String filename = "/test/xml/generic/oro-eprint-25656.xml";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }

        NamespaceContext context = SimpleNamespaceContext.newInstance();
        context.addNamespace("oaidc", "http://www.openarchives.org/OAI/2.0/oai_dc/");

        XmlHandler handler = new XmlResourceHandler(src, context) {

            @Override
            public boolean isResourceDelimiter(QName name) {
                return "oai_dc".equals(name.getLocalPart());
            }

            @Override
            public URI identify(QName name, String value, URI identifier) {
                if ("identifier".equals(name.getLocalPart())) {
                    try {
                        return URI.create(value);
                    } catch (Exception e) {
                    }
                }
                return null;
            }
            
            @Override
            public boolean skip(QName name) {
                return "dc".equals(name.getLocalPart());
            }

        };
        handler.setListener(new ResourceBuilder());
        new XmlReader().setHandler(handler).parse(new InputSource(in));
        StringWriter sw = new StringWriter();
        TurtleWriter t = new TurtleWriter();
        t.write(src.resource(), true, sw);
        logger.log(Level.INFO, sw.toString());
    }

    class ResourceBuilder implements StatementListener {

        @Override
        public void newIdentifier(URI uri) {
            logger.log(Level.INFO, "uri = {0}", uri.toString());
            src.resource().setIdentifier(uri);
        }

        @Override
        public void statement(Statement statement) {
            logger.log(Level.INFO, "statement = {0}", statement.toString());
            src.resource().add(statement);
        }
    }
}
