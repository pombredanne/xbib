package org.xbib.common.xcontent;

import com.google.common.io.ByteStreams;
import org.testng.annotations.Test;
import org.xbib.common.xcontent.xml.XmlXParams;
import org.xbib.logging.Logger;
import org.xbib.logging.Loggers;
import org.xbib.xml.XMLNamespaceContext;

import javax.xml.namespace.QName;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.xbib.common.xcontent.XContentFactory.xmlBuilder;

public class XmlBuilderTest {

    private final Logger logger = Loggers.getLogger(XmlBuilderTest.class);

    @Test
    public void testXml() throws Exception {
        XmlXParams params = new XmlXParams(new QName("root"));
        XContentBuilder builder = xmlBuilder(params);
        builder.startObject().field("Hello", "World").endObject();
        logger.info("xml = {}", builder.string());
    }

    @Test
    public void testXmlNamespaces() throws Exception {
        // dc is in default namespace
        QName root = XmlXParams.getDefaultParams().getQName();
        XmlXParams params = new XmlXParams(root);
        XContentBuilder builder = xmlBuilder(params);
        builder.startObject()
                .field("dc:creator", "John Doe")
                .endObject();
        logger.info("xml (namespaces) = {}", builder.string());
    }

    @Test
    public void testXmlCustomNamespaces() throws Exception {
        QName root = XmlXParams.getDefaultParams().getQName();
        XMLNamespaceContext nsContext = XMLNamespaceContext.getInstance();
        nsContext.addNamespace("abc", "http://localhost");
        XmlXParams params = new XmlXParams(root, nsContext);
        XContentBuilder builder = xmlBuilder(params);
        builder.startObject()
                .field("abc:creator", "John Doe")
                .endObject();
        logger.info("xml (custom namespaces) = {}", builder.string());
    }

    @Test
    public void testXmlObject() throws Exception {
        QName root = XmlXParams.getDefaultParams().getQName();
        XmlXParams params = new XmlXParams(root);
        XContentBuilder builder = xmlBuilder(params);
        builder.startObject()
                .startObject("author")
                .field("creator", "John Doe")
                .field("role", "writer")
                .endObject()
                .startObject("author")
                .field("creator", "Joe Smith")
                .field("role", "illustrator")
                .endObject()
                .endObject();
        logger.info("xml (objects) = {}", builder.string());
    }

    @Test
    public void testXmlAttributes() throws Exception {
        QName root = XmlXParams.getDefaultParams().getQName();
        XmlXParams params = new XmlXParams(root);
        XContentBuilder builder = xmlBuilder(params);
        builder.startObject()
                .startObject("author")
                .field("@name", "John Doe")
                .field("@id", 1)
                .endObject()
                .endObject();
        logger.info("xml (attribute) = {}", builder.string());
    }


    @Test
    public void testXmlArrayOfValues() throws Exception {
        QName root = XmlXParams.getDefaultParams().getQName();
        XmlXParams params = new XmlXParams(root);
        XContentBuilder builder = xmlBuilder(params);
        builder.startObject()
                .array("author", "John Doe", "Joe Smith")
                .endObject();
        logger.info("xml (array of values) = {}", builder.string());
    }

    @Test
    public void testXmlArrayOfObjects() throws Exception {
        QName root = XmlXParams.getDefaultParams().getQName();
        XmlXParams params = new XmlXParams(root);
        XContentBuilder builder = xmlBuilder(params);
        builder.startObject()
                .startArray("author")
                .startObject()
                .field("creator", "John Doe")
                .field("role", "writer")
                .endObject()
                .startObject()
                .field("creator", "Joe Smith")
                .field("role", "illustrator")
                .endObject()
                .endArray()
                .endObject();
        logger.info("xml (array of objects) = {}", builder.string());
    }

    @Test
    public void testParseJson() throws Exception {
        InputStream in = getClass().getResourceAsStream("/org/xbib/json/dc.json");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteStreams.copy(in, out);
        byte[] buf = out.toByteArray();
        logger.info("from json = {}", XContentHelper.convertToXml(buf, 0, buf.length, true));
    }

}
