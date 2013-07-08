package org.xbib.rdf.io.xml;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.Loggers;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.text.CharUtils;
import org.xbib.text.UrlEncoding;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class OAITest extends Assert {

    private final Logger logger = Loggers.getLogger(OAITest.class);

    @Test
    public void testOAIListRecords() throws Exception {
        String filename = "/org/xbib/rdf/io/xml/oai-listrecords.xml";
        InputStream in = getClass().getResourceAsStream(filename);
        if (in == null) {
            throw new IOException("file " + filename + " not found");
        }

        IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace("oaidc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");

        final SimpleResourceContext resourceContext = new SimpleResourceContext();
        resourceContext.newNamespaceContext(context);

        XmlHandler xmlHandler = new AbstractXmlResourceHandler(resourceContext) {

            @Override
            public boolean isResourceDelimiter(QName name) {
                return "oai_dc".equals(name.getLocalPart());
            }

            @Override
            public void identify(QName name, String value, IRI identifier) {
                if ("identifier".equals(name.getLocalPart()) && identifier == null) {
                    // make sure we can build an opaque IRI, whatever is out there
                    String s = UrlEncoding.encode(value, CharUtils.Profile.SCHEMESPECIFICPART.filter());
                    resourceContext.resource().id(IRI.create("id:" + s));
                }
            }

            @Override
            public boolean skip(QName name) {
                if (name.getLocalPart().startsWith("@")) {
                    return true;
                }
                return false;
            }

        };
        StringWriter sw = new StringWriter();
        TurtleWriter t = new TurtleWriter()
                .output(sw)
                .setContext(context)
                .writeNamespaces();
        xmlHandler.setListener(t)
            .setDefaultNamespace("oai", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        new XmlReader()
                .setHandler(xmlHandler)
                .parse(new InputSource(in));
        t.close();
        String s = sw.toString().trim();
        logger.info(s);
    }
}
