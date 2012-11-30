package org.xbib.rdf.io.xml;

import org.xbib.rdf.io.xml.XmlHandler;
import org.xbib.rdf.io.xml.XmlResourceHandler;
import org.xbib.rdf.io.xml.XmlReader;
import org.xbib.rdf.io.turtle.TurtleWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import javax.xml.namespace.QName;
import org.testng.annotations.Test;
import org.xbib.rdf.Statement;
import org.xbib.rdf.io.StatementListener;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.xml.NamespaceContext;
import org.xbib.xml.SimpleNamespaceContext;
import org.xml.sax.InputSource;

public class XmlReaderTest {

    private final SimpleResourceContext src = new SimpleResourceContext();

    @Test
    public void testGenericXmlReader() throws Exception {
        String filename = "/org/xbib/rdf/io/oro-eprint-25656.xml";
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
        //logger.info(sw.toString());
    }

    class ResourceBuilder implements StatementListener {

        @Override
        public void newIdentifier(URI uri) {
            //logger.info("uri = {}", uri.toString());
            src.resource().id(uri);
        }

        @Override
        public void statement(Statement statement) {
            //logger.info("statement = {}", statement.toString());
            src.resource().add(statement);
        }
    }
}
