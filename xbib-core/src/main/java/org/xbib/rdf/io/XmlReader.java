package org.xbib.rdf.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Iterator;
import java.util.Stack;
import javax.xml.namespace.QName;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.xml.NamespaceContext;
import org.xbib.xml.SimpleNamespaceContext;
import org.xbib.xml.transform.XMLFilterReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class XmlReader<S extends Resource<S, P, O>, P extends Property, O extends Literal<O>>
        implements XmlTriplifier<S, P, O> {

    private NamespaceContext context;
    private NamespaceContext ignore;
    private StatementListener listener;
    private Resource<S, P, O> root;
    private Resource<S, P, O> resource;
    private URI identifier;

    public XmlReader() {
        this(SimpleNamespaceContext.getInstance());
    }

    public XmlReader(NamespaceContext context) {
        this.context = context;
    }

    public XmlReader setIdentifier(URI identifier) {
        this.identifier = identifier;
        return this;
    }

    public XmlReader setIgnoreNamespaces(NamespaceContext ignore) {
        this.ignore = ignore;
        return this;
    }

    public Resource getResource() {
        return root;
    }

    /**
     * Set the triple listener that gets called every time a triple is read.
     *
     * @param listener the triple listener
     */
    @Override
    public XmlReader setListener(StatementListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Get the triple listener
     *
     * @return the triple listener
     */
    @Override
    public StatementListener getListener() {
        return listener;
    }

    @Override
    public XmlReader parse(InputStream in) throws IOException {
        return parse(new InputStreamReader(in, "UTF-8"));
    }

    @Override
    public XmlReader parse(Reader reader) throws IOException {
        try {
            parse(new InputSource(reader));
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
        return this;
    }

    @Override
    public XmlReader parse(InputSource source) throws IOException, SAXException {
        parse(XMLReaderFactory.createXMLReader(), source);
        return this;
    }

    @Override
    public XmlReader parse(XMLReader reader, InputSource source) throws IOException, SAXException {
        XmlHandler xmlHandler = new Handler();
        reader.setContentHandler(xmlHandler);
        reader.parse(source);
        return this;
    }

    @Override
    public XmlReader parse(XMLFilterReader reader, InputSource source) throws IOException, SAXException {
        XmlHandler xmlHandler = new Handler();
        reader.setContentHandler(xmlHandler);
        reader.parse(source);
        return this;
    }

    @Override
    public XmlHandler getHandler() {
        return new Handler();
    }

    private String prefix(String name) {
        return name.replaceAll("[^a-zA-Z]+", "");
    }

    class Handler extends XmlHandler {

        StringBuilder content = new StringBuilder();
        Stack<QName> stack = new Stack();
        Stack<Resource> resources = new Stack();

        @Override
        public void startDocument() throws SAXException {
            resource = new SimpleResource();
            root = resource;
        }

        @Override
        public void endDocument() throws SAXException {
            root.setIdentifier(identifier);
            listener.newIdentifier(identifier);
            Iterator<Statement<S, P, O>> it = root.iterator(true);
            while (it.hasNext()) {
                Statement<S, P, O> st = it.next();
                listener.statement(st);
            }
            root = null;
        }

        @Override
        public void startElement(String nsURI, String localname, String qname, Attributes atts) throws SAXException {
            if (ignore != null && ignore.getPrefix(nsURI) != null) {
                return;
            }
            if (!stack.empty()) {
                QName name = stack.peek();
                URI uri = URI.create(prefix(name.getPrefix()) + ":" + name.getLocalPart());
                P property = root.createPredicate(uri);
                resources.push(resource);
                resource = resource.createResource(property);
            }
            stack.push(new QName(nsURI, localname, context.getPrefix(nsURI)));
            content.setLength(0);
        }

        @Override
        public void endElement(String nsURI, String localname, String qname) throws SAXException {
            if (ignore != null && ignore.getPrefix(nsURI) != null) {
                return;
            }
            QName name = stack.pop();
            URI uri = URI.create(prefix(name.getPrefix()) + ":" + name.getLocalPart());
            P property = root.createPredicate(uri);
            if (content.length() > 0) {
                resource.addProperty(property, content.toString());
            }
            if (!resources.empty()) {
                resource = resources.pop();
            }
        }

        @Override
        public void characters(char[] chars, int start, int length) throws SAXException {
            content.append(new String(chars, start, length));
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            if (ignore != null && ignore.getPrefix(uri) != null) {
                return;
            }
            context.addNamespace(prefix(prefix), uri);
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
        }
    }
}
