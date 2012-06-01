package org.xbib.elasticsearch.xcontent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Stack;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import org.apache.lucene.util.UnicodeUtil;
import org.codehaus.jackson.JsonToken;
import org.elasticsearch.common.Unicode;
import org.elasticsearch.common.xcontent.XContentParser;
import org.xbib.elasticsearch.xcontent.events.SAXEvent;
import org.xbib.xml.NamespaceContext;
import org.xbib.xml.SimpleNamespaceContext;
import org.xbib.xml.XMLUtil;
import org.xml.sax.ContentHandler;

public class XmlGenerator {

    private final static String DEFAULT_ROOT = "xml:root";
    private Writer writer;
    private final QName root;
    private final NamespaceContext context;
    private final ContentHandler handler;
    private QName qname;
    private SAXEvent event;
    private Stack<SAXEvent> events = new Stack();
    private Stack<QName> elements = new Stack();
    private Stack tokens = new Stack();
    private boolean pretty = true;
    private boolean namespaceDecls = true;
    private Transformer transformer;

    public XmlGenerator() {
        this.root = toQName(DEFAULT_ROOT);
        this.context = SimpleNamespaceContext.getInstance();
        this.handler = null;
    }

   public XmlGenerator(OutputStream out)
            throws IOException {
        this(null, new OutputStreamWriter(out, "UTF-8"), SimpleNamespaceContext.getInstance(), null);
    }

    public XmlGenerator(Writer writer)
            throws IOException {
        this(null, writer, SimpleNamespaceContext.getInstance(), null);
    }
     
    public XmlGenerator(QName root, OutputStream out)
            throws IOException {
        this(root, new OutputStreamWriter(out, "UTF-8"), SimpleNamespaceContext.getInstance(), null);
    }

    public XmlGenerator(QName root, Writer writer)
            throws IOException {
        this(root, writer, SimpleNamespaceContext.getInstance(), null);
    }

    public XmlGenerator(QName root, Writer writer, NamespaceContext context, ContentHandler handler)
            throws IOException {
        this.root = root == null? toQName(DEFAULT_ROOT) : root;
        this.writer = writer;
        this.context = context;
        this.handler = handler;
        this.qname = root;
        try {
            this.transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException ex) {
            throw new IOException(ex);
        }
    }

    public void usePrettyPrint() {
        this.pretty = true;
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    }

    public void writeStartArray() throws IOException {
        tokens.push(JsonToken.START_ARRAY);
    }

    public void writeEndArray() throws IOException {
    }

    public void writeStartObject() throws IOException {
        tokens.push(JsonToken.START_OBJECT);
        //consumer.add(eventFactory.createStartElement(qname, null, null));
        if (namespaceDecls) {
            if (!context.getNamespaceMap().containsKey(qname.getPrefix())) {
                //consumer.add(eventFactory.createNamespace(qname.getPrefix(), qname.getNamespaceURI()));
            }
            for (String prefix : context.getNamespaceMap().keySet()) {
                String namespaceURI = context.getNamespaceURI(prefix);
                //consumer.add(eventFactory.createNamespace(prefix, namespaceURI));
            }
            namespaceDecls = false;
        }
        elements.push(qname);
    }

    public void writeEndObject() throws IOException {
        qname = elements.pop();
        //consumer.add(eventFactory.createEndElement(qname, null));        
    }

    public void writeFieldName(String name) throws IOException {
        qname = toQName(name);
    }

    public void writeString(String text) throws IOException {
        int len = text.length();
        if (len == 0) {
            // 
        } else {
            //consumer.add(eventFactory.createStartElement(qname, null, null));
            //consumer.add(eventFactory.createCharacters(text));
            //consumer.add(eventFactory.createEndElement(qname, null));
        }
    }
    
    public void writeRawField(String fieldName, byte[] content, OutputStream out) throws IOException {
        StringBuilder sb = new StringBuilder();
        CharSequence s = XMLUtil.escape(new String(content));
        sb.append('<').append(fieldName).append('>').append(s).append("</").append(fieldName).append('>');
        UnicodeUtil.UTF8Result result = Unicode.unsafeFromStringAsUtf8(sb.toString());
        out.write(result.result);
        
    }

    public void writeRawField(String fieldName, byte[] content, int offset, int length, OutputStream bos) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void writeRawField(String fieldName, InputStream content, OutputStream bos) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void copyCurrentStructure(XmlParser parser) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void flush() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void close() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    

    private QName toQName(String name) {
        String nsPrefix = root.getPrefix();
        String nsURI = root.getNamespaceURI();
        // convert all JSON names beginning with an underscore to elements in default namespace
        if (name.startsWith("_")) {
            name = name.substring(1);
        }
        int pos = name.indexOf(':');
        if (pos > 0) {
            // check for configured namespace
            nsPrefix = name.substring(0, pos);
            nsURI = context.getNamespaceURI(nsPrefix);
            if (nsURI == null) {
                throw new IllegalArgumentException("unknown namespace prefix: " + nsPrefix);
            }
            name = name.substring(pos + 1);
        }
        return new QName(nsURI, name, nsPrefix);
    }
}
