package org.elasticsearch.common.xcontent.xml;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.common.xcontent.XContentBuilder;
import static org.elasticsearch.common.xcontent.XContentFactory.xmlBuilder;
import org.testng.annotations.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlTest {

    private static final Logger logger = Logger.getLogger(XmlTest.class.getName());

    @Test
    public void testXmlObject() throws IOException {

        XContentBuilder builder = xmlBuilder();
        builder.startObject().field("hello", "World").endObject();
        System.err.println("builder=" + builder.string());
    }

    @Test
    public void testXmlArray() throws IOException {

        XContentBuilder builder = xmlBuilder();
        builder.startObject().array("test", "Hello", "World").endObject();
        System.err.println("builder=" + builder.string());
    }

    @Test
    public void testXmlHandler() throws IOException {
        DefaultHandler handler = new DefaultHandler() {

            @Override
            public void startDocument() throws SAXException {
                logger.log(Level.INFO, "start document");
            }

            @Override
            public void endDocument() throws SAXException {
                logger.log(Level.INFO, "end document");
            }

            @Override
            public void startPrefixMapping(String string, String string1) throws SAXException {
                logger.log(Level.INFO, "start prefix mapping " + string + " " + string1);
            }

            @Override
            public void endPrefixMapping(String string) throws SAXException {
                logger.log(Level.INFO, "end prefix mapping");
            }

            @Override
            public void startElement(String ns, String localname, String string2, Attributes atrbts) throws SAXException {
                logger.log(Level.INFO, "start element " + ns + " " + localname);
            }

            @Override
            public void endElement(String ns, String localname, String string2) throws SAXException {
                logger.log(Level.INFO, "end element " + ns +  " " + localname);
            }

            @Override
            public void characters(char[] chars, int i, int i1) throws SAXException {
                logger.log(Level.INFO, "character " + new String(chars, i, i1));
            }
        };
        XContentBuilder builder = xmlBuilder(handler);
        builder.startObject().array("test", "Hello", "World").endObject();
        System.err.println("builder=" + builder.string());
    }
}
