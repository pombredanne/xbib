package org.xbib.rdf.io.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.namespace.QName;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.rdf.Statement;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.io.StatementListener;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.text.CharUtils.Profile;
import org.xbib.text.UrlEncoding;
import org.xml.sax.InputSource;

public class XmlReaderTest extends Assert {

    private final SimpleResourceContext resourceContext = new SimpleResourceContext();

    @Test
    public void testGenericXmlReader() throws Exception {
        String filename = "/org/xbib/rdf/io/oro-eprint-25656.xml";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }

        IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace("oaidc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        resourceContext.newNamespaceContext(context);

        AbstractXmlHandler handler = new XmlResourceHandler(resourceContext) {

            @Override
            public boolean isResourceDelimiter(QName name) {
                return "oai_dc".equals(name.getLocalPart());
            }

            @Override
            public void identify(QName name, String value, IRI identifier) {
                if ("identifier".equals(name.getLocalPart()) && identifier == null) {
                    // make sure we can build an opaque IRI, whatever is out there
                    String s = UrlEncoding.encode(value, Profile.SCHEMESPECIFICPART.filter());
                    resourceContext.resource().id(IRI.create("id:" + s));
                }
            }
            
            @Override
            public boolean skip(QName name) {
                // skip dc:dc element
                return "dc".equals(name.getLocalPart());
            }

        };
        handler.setListener(new ResourceBuilder());
        new XmlReader().setHandler(handler).parse(new InputSource(in));
        StringWriter sw = new StringWriter();
        TurtleWriter t = new TurtleWriter();
        t.write(resourceContext.resource(), true, sw);
        assertEquals(sw.toString().length(), 1877);
    }

    class ResourceBuilder implements StatementListener {

        @Override
        public void newIdentifier(IRI uri) {
            resourceContext.resource().id(uri);
        }

        @Override
        public void statement(Statement statement) {
            resourceContext.resource().add(statement);
        }
    }
}
