/*
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.rdf.io.rdfxml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.xbib.iri.IRI;
import org.xbib.iri.IRISyntaxException;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.ResourceContext;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.io.xml.XmlHandler;
import org.xbib.rdf.simple.SimpleFactory;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.RDF;
import org.xbib.rdf.io.xml.XmlTriplifier;
import org.xbib.rdf.IdentifiableNode;
import org.xbib.rdf.simple.SimpleLiteral;
import org.xbib.rdf.simple.SimpleTriple;
import org.xbib.xml.XMLFilterReader;
import org.xbib.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * RdfXmlParser is an admittedly convoluted hand-coded SAX parser for RDF/XML.
 * This is designed to be faster than wrapping Jena's parser and should obviate
 * the need to add Jena as a dependency. It should also be able to process
 * arbitrarily large RDF/XML files with minimal memory overhead, since unlike
 * Jena it does not have to store and index all the triples it encounters in a
 * model.
 *
 * Note that the XMLLiteral datatype is not fully supported.
 *
 */
public class RdfXmlReader<S extends Identifier, P extends Property, O extends Node>
        implements RDF, XmlTriplifier<S,P,O> {

    private final Logger logger = LoggerFactory.getLogger(RdfXmlReader.class.getName());

    private final SimpleFactory<S,P,O> simpleFactory = SimpleFactory.getInstance();

    private XmlHandler xmlHandler = new Handler();

    private TripleListener<S,P,O> listener;

    // counter for blank node generation
    private int bn = 0;

    @Override
    public RdfXmlReader parse(InputStream in) throws IOException {
        return parse(new InputStreamReader(in, "UTF-8"));
    }
    
    @Override
    public RdfXmlReader parse(Reader reader) throws IOException {
        try {
            parse(new InputSource(reader));
        } catch (SAXException ex) {
            throw new IOException(ex);
        }
        return this;
    } 

    @Override
    public RdfXmlReader parse(InputSource source) throws IOException, SAXException {
        parse(XMLReaderFactory.createXMLReader(), source);
        return this;
    }

    @Override
    public RdfXmlReader parse(XMLReader reader, InputSource source) throws IOException, SAXException {
        xmlHandler.setListener(listener);
        reader.setContentHandler(xmlHandler);
        reader.parse(source);
        return this;
    }

    @Override
    public RdfXmlReader parse(XMLFilterReader reader, InputSource source) throws IOException, SAXException {
        xmlHandler.setListener(listener);
        reader.setContentHandler(xmlHandler);
        reader.parse(source);
        return this;
    }

    public RdfXmlReader<S,P,O> setHandler(XmlHandler handler) {
        this.xmlHandler = handler;
        return this;
    }

    @Override
    public RdfXmlReader setTripleListener(TripleListener<S,P,O> tripleHandler) {
        this.listener = tripleHandler;
        return this;
    }

    @Override
    public XmlHandler getHandler() {
        return xmlHandler;
    }

    private void yield(Object s, Object p, Object o) {
        yield(new SimpleTriple(simpleFactory.asSubject(s), simpleFactory.asPredicate(p), simpleFactory.asObject(o)));
    }

    private void yield(S s, P p, O o) {
        yield(new SimpleTriple(s, p, o));
    }

    // produce a triple for the listener
    private void yield(Triple t) {
        if (listener != null) {
            listener.triple(t);
        }
    }

    // produce a (possibly) reified triple
    private void yield(Object s, IRI p, Object o, IRI reified) {
        yield(s, p, o);
        if (reified != null) {
            yield(reified, RDF_TYPE, RDF_STATEMENT);
            yield(reified, RDF_SUBJECT, s);
            yield(reified, RDF_PREDICATE, p);
            yield(reified, RDF_OBJECT, o);
        }
    }

    // get the most-specific langauge tag in scope
    private String getLanguage(Stack<Frame> stack) {
        String lang = "";
        for (Frame frame : stack) {
            if (frame.lang != null && !lang.startsWith(frame.lang)) {
                lang = frame.lang;
            }
        }
        return lang;
    }

    // get the xml:base in scope
    private String getBase(Stack<Frame> stack) {
        String base = "";
        for (Frame frame : stack) {
            if (frame.base != null) {
                base = frame.base;
            }
        }
        return base;
    }

    // is our parent a predicate?
    private boolean inPredicate(Stack<Frame> stack) {
        boolean ip = false;
        for (Frame frame : stack) {
            ip = frame.isPredicate;
        }
        return ip;
    }

    // do we expect to encouter a subject (rather than a predicate?)
    private boolean expectSubject(Stack<Frame> stack) {
        boolean es = true;
        for (Frame frame : stack) {
            es = !frame.isSubject;
        }
        return es;
    }

    // if we're in a predicate, get its frame
    private Frame parentPredicateFrame(Stack<Frame> stack) throws SAXException {
        if (inPredicate(stack)) {
            Frame predicateFrame = null;
            for (Frame frame : stack) {
                if (frame.isPredicate) {
                    predicateFrame = frame;
                }
            }
            return predicateFrame;
        } else {
            throw new SAXException("internal parser error: cannot find enclosing predicate");
        }
    }

    // get the uriRef of the predicate we're in
    private IRI parentPredicate(Stack<Frame> stack) throws SAXException {
        Frame ppFrame = parentPredicateFrame(stack);
        return ppFrame != null ? ppFrame.node : null;
    }

    // get the nearest ancestor subject frame
    private Frame ancestorSubjectFrame(Stack<Frame> stack) throws SAXException {
        Frame subjectFrame = null;
        for (Frame frame : stack) {
            if (frame.isSubject) {
                subjectFrame = frame;
            }
        }
        return subjectFrame;
    }

    // get the nearest ancestor subject
    private IRI ancestorSubject(Stack<Frame> stack) throws SAXException {
        Frame subjectFrame = ancestorSubjectFrame(stack);
        return subjectFrame != null ? subjectFrame.node : null;
    }

    // if we're looking at a subject, is it an item in a Collection?
    private boolean isCollectionItem(Stack<Frame> stack) throws SAXException {
        if (inPredicate(stack)) {
            Frame predicateFrame = parentPredicateFrame(stack);
            return predicateFrame != null && predicateFrame.isCollection;
        } else {
            return false;
        }
    }

    private Identifier blankNode() {
        return new IdentifiableNode().id("b" + (bn++));
    }

    private Identifier blankNode(String s) {
        return new IdentifiableNode().id(s);
    }

    /*
     * Resolve relative uri's against the in-scope xml:base URI.
     * IRI creation/parsing comes with very weak performance.
     */
    private IRI resolve(String uriString, Stack<Frame> stack) {
        IRI uri;
        try {
            uri = IRI.create(uriString);
        } catch (IRISyntaxException e) {
            // illegal URI, try repair
            uri = IRI.create(uriString
                  .replace(" ", "%20")
                  .replace("\"", "%22")
                  .replace("[", "%5B")
                  .replace("]", "%5D")
                  .replace("<", "%3C")
                  .replace(">", "%3E")
                  .replace("|", "%7C")
                  .replace("`", "%60")
            );
        }
        if (uri.isAbsolute()) {
            return uri;
        } else {
            return IRI.create(getBase(stack) + uriString);
        }
    }

    /**
     *
     *  The complicated logic to determine the subject uri ref
     */
    private void getSubjectNode(Frame frame, Stack<Frame> stack, Attributes attrs) throws SAXException {
        String about = attrs.getValue(RDF.toString(), "about");
        if (about != null) {
            frame.node = resolve(about, stack);
            if (listener != null) {
                listener.newIdentifier(frame.node);
            }
        }
        String nodeId = attrs.getValue(RDF.toString(), "nodeID");
        if (nodeId != null) {
            if (frame.node != null) {
                throw new SAXException("ambiguous use of rdf:nodeID");
            }
            frame.node = blankNode(nodeId).id();
        }
        String rdfId = attrs.getValue(RDF.toString(), "ID");
        if (rdfId != null) {
            if (frame.node != null) {
                throw new SAXException("ambiguous use of rdf:ID");
            }
            frame.node = IRI.create(getBase(stack) + "#" + rdfId);
        }
        if (frame.node == null) {
            frame.node = blankNode().id();
        }
        frame.isSubject = true;
    }

    // the complicated logic to deal with attributes with rdf:resource, nodeID attrs
    private IRI getObjectNode(Stack<Frame> stack, Attributes attrs) throws SAXException {
        IRI node = null;
        String resource = attrs.getValue(RDF.toString(), "resource");
        if (resource != null) {
            node = resolve(resource, stack);
        }
        String nodeId = attrs.getValue(RDF.toString(), "nodeID");
        if (nodeId != null) {
            if (node != null) {
                throw new SAXException("ambiguous use of rdf:nodeID");
            }
            node = blankNode(nodeId).id();
        }
        return node;
    }

    // here we're in a literal so we have to produce reasonably-canonical XML
    // representation of this start tag
    private void xmlLiteralStart(StringBuilder out, String ns, String qn, Attributes attrs) {
        out.append("<").append(qn);
        Map<String, String> pfxMap = new HashMap<>();
        for (int i = -1; i < attrs.getLength(); i++) {
            String aQn, aNs;
            if (i < 0) {
                aQn = qn;
                aNs = ns;
            } else {
                aQn = attrs.getQName(i);
                aNs = attrs.getURI(i);
            }
            if (!"".equals(aNs)) {
                String pfx = aQn.replaceFirst(":.*", "");
                pfxMap.put(pfx, aNs);
            }
        }
        for (Map.Entry<String, String> pfxMapping : pfxMap.entrySet()) {
            out.append(" xmlns:").append(pfxMapping.getKey()).append("=\"").append(pfxMapping.getValue()).append("\"");
        }
        for (int i = 0; i < attrs.getLength(); i++) {
            String aQn = attrs.getQName(i);
            String aVal = attrs.getValue(i);
            out.append(" ").append(aQn).append("=\"").append(XMLUtil.escape(aVal)).append("\"");
        }
        out.append(">");
    }

    // produce a reasonably canonical end tag
    private void xmlLiteralEnd(StringBuilder out, String qn) {
        out.append("</").append(qn).append(">");
    }

    // if a language tag is in scope, apply it to the literal
    private Literal withLanguageTag(Literal l, Stack<Frame> stack) {
        String lang = getLanguage(stack);
        if (!"".equals(lang)) {
            l.language(lang);
        }
        return l;
    }

    class Frame {

        IRI node = null; // the subject/object
        String lang = null; // the language tag
        String base = null; // the xml:base
        String datatype = null; // a predicate's datatype
        IRI reification = null; // when reifying, the triple's uriRef
        List<IRI> collection = null; // for parseType=Collection, the items
        Triple<S, P, O> collectionHead = null; // for parseType=Collection, the head triple
        boolean isSubject = false; // is there a subject at this frame
        boolean isPredicate = false; // is there a predicate at this frame
        boolean isCollection = false; // is the predicate at this frame a collection
        int li = 1;
    }

    class Handler extends DefaultHandler implements XmlHandler {

        private Stack<Frame> stack = new Stack<>();

        private StringBuilder pcdata = null;

        private StringBuilder xmlLiteral = null;

        private TripleListener listener;

        private int literalLevel = 0; // level in XMLLiteral

        @Override
        public Handler setDefaultNamespace(String prefix, String namespaceURI) {
            return this;
        }

        @Override
        public Handler setListener(TripleListener listener) {
            this.listener = listener;
            return this;
        }

        @Override
        public ResourceContext resourceContext() {
            return null;
        }

        @Override
        public void startPrefixMapping (String prefix, String uri)
                throws SAXException {
            if (listener != null) {
                listener.startPrefixMapping(prefix, uri);
            }
        }

        @Override
        public void endPrefixMapping (String prefix)
                throws SAXException {
            if (listener != null) {
                listener.endPrefixMapping(prefix);
            }
        }

        @Override
        public void startElement(String ns, String name, String qn, Attributes attrs) throws SAXException {
            if (literalLevel > 0) { // this isn't RDF; we're in an XMLLiteral
                literalLevel++;
                // now produce an equivalent start tag
                xmlLiteralStart(xmlLiteral, ns, qn, attrs);
            } else { // we're in RDF
                Frame frame = new Frame();
                IRI uri = IRI.create(ns + name);
                frame.lang = attrs.getValue("xml:lang");
                frame.base = attrs.getValue("xml:base");
                if (expectSubject(stack)) {
                    if (!uri.equals(RDF_RDF)) {
                        // get this resource's ID
                        getSubjectNode(frame, stack, attrs);
                        // we have the subject
                        if (!uri.equals(RDF_DESCRIPTION)) {
                            // this is a typed node, so assert the type
                            yield(frame.node, RDF_TYPE, uri);
                        }
                        // now process attribute-specified predicates
                        for (int i = 0; i < attrs.getLength(); i++) {
                            String aQn = attrs.getQName(i);
                            String aUri = attrs.getURI(i) + attrs.getLocalName(i);
                            String aVal = attrs.getValue(i);
                            if (aUri.startsWith(RDF.toString())
                                    || aQn.startsWith("xml:")) {
                                // skip
                            } else {
                                yield(frame.node, IRI.create(aUri), aVal);
                            }
                        }
                        // is this node the value of some enclosing predicate?
                        if (inPredicate(stack)) {
                            // is the value of the predicate a collection?
                            if (isCollectionItem(stack)) {
                                Frame ppFrame = parentPredicateFrame(stack);
                                ppFrame.collection.add(frame.node);
                            } else { // not a collection
                                // this subject is the value of its enclosing predicate
                                yield(ancestorSubject(stack), parentPredicate(stack), frame.node);
                            }
                        }
                    }
                    // do not accumulate pcdata
                    pcdata = null;
                } else { // expect predicate
                    frame.node = uri;
                    frame.isPredicate = true;
                    // handle reification
                    String reification = attrs.getValue(RDF.toString(), "ID");
                    if (reification != null) {
                        frame.reification = IRI.create(getBase(stack) + "#" + reification);
                    }
                    // handle container items
                    if (uri.equals(RDF_LI)) {
                        Frame asf = ancestorSubjectFrame(stack);
                        frame.node = IRI.create(RDF + "_" + asf.li);
                        asf.li++;
                    }
                    // parse attrs to see if the value of this pred is a uriref
                    IRI object = getObjectNode(stack, attrs);
                    if (object != null) {
                        yield(ancestorSubject(stack), frame.node, object, frame.reification);
                    } else {
                        // this predicate encloses pcdata, prepare to accumulate
                        pcdata = new StringBuilder();
                    }
                    // handle rdf:parseType="resource"
                    String parseType = attrs.getValue(RDF.toString(), "parseType");
                    if (parseType != null) {
                        switch (parseType) {
                            case "Resource":
                                object = object == null ? blankNode().id() : object;
                                yield(ancestorSubject(stack), frame.node, object, frame.reification);
                                // perform surgery on the current frame
                                frame.node = object;
                                frame.isSubject = true;
                                break;
                            case "Collection":
                                frame.isCollection = true;
                                frame.collection = new LinkedList();
                                S s = simpleFactory.asSubject(ancestorSubject(stack));
                                P p = simpleFactory.asPredicate(frame.node);
                                O o = simpleFactory.asObject(blankNode());
                                frame.collectionHead = new SimpleTriple(s,p,o);
                                pcdata = null;
                                break;
                            case "Literal":
                                literalLevel = 1; // enter into a literal
                                xmlLiteral = new StringBuilder();
                                // which means we shouldn't accumulate pcdata!
                                pcdata = null;
                                break;
                            default:
                                // handle datatype
                                frame.datatype = attrs.getValue(RDF.toString(), "datatype");
                                break;
                        }
                    }
                    // now handle property attributes (if we do this, then this
                    // must be an empty element)
                    object = null;
                    for (int i = 0; i < attrs.getLength(); i++) {
                        String aQn = attrs.getQName(i);
                        IRI aUri = IRI.create(attrs.getURI(i) + attrs.getLocalName(i));
                        String aVal = attrs.getValue(i);
                        if ((!aUri.toString().equals(RDF_TYPE.toString()) && aUri.toString().startsWith(RDF.toString()))
                                || aQn.startsWith("xml:")) {
                            // ignore
                        } else {
                            if (object == null) {
                                object = blankNode().id();
                                yield(ancestorSubject(stack), frame.node, object);
                            }
                            if (aUri.equals(RDF_TYPE)) {
                                yield(object, RDF_TYPE, aVal);
                            } else {
                                Literal value = withLanguageTag(new SimpleLiteral(aVal), stack);
                                yield(object, aUri, value);
                            }
                        }
                    }
                    // if we had to generate a node to hold properties specified
                    // as attributes, then expect an empty element and therefore
                    // don't record pcdata
                    if (object != null) {
                        pcdata = null;
                    }
                }
                // finally, push the frame for use in subsequent callbacks
                stack.push(frame);
            }
        }

        @Override
        public void endElement(String ns, String name, String qn) throws SAXException {
            if (literalLevel > 0) { // this isn't RDF; we're in an XMLLiteral
                literalLevel--;
                if (literalLevel > 0) {
                    xmlLiteralEnd(xmlLiteral, qn);
                }
            } else { // this is RDF
                if (inPredicate(stack)) {
                    Frame ppFrame = parentPredicateFrame(stack);
                    // this is a predicate closing
                    if (xmlLiteral != null) { // it was an XMLLiteral
                        Literal value = new SimpleLiteral(xmlLiteral.toString()).type(RDF_XMLLITERAL);
                        yield(ancestorSubject(stack), parentPredicate(stack), value);
                        xmlLiteral = null;
                    } else if (pcdata != null) { // we have an RDF literal
                        IRI u = ppFrame.datatype == null ? null : IRI.create(ppFrame.datatype);
                        Literal value = withLanguageTag(new SimpleLiteral(pcdata.toString()).type(u), stack);
                        // deal with reification
                        IRI reification = ppFrame.reification;
                        yield(ancestorSubject(stack), ppFrame.node, value, reification);
                        // no longer collect pcdata
                        pcdata = null;
                    } else if (ppFrame.isCollection) { // deal with collections
                        if (ppFrame.collection.isEmpty()) {
                            // in this case, the value of this property is rdf:nil
                            yield(ppFrame.collectionHead.subject(),
                                    ppFrame.collectionHead.predicate(),
                                    simpleFactory.asObject(RDF_NIL));
                        } else {
                            yield(ppFrame.collectionHead);
                            Object prevNode = null;
                            Object node = ppFrame.collectionHead.object();
                            for (IRI item : ppFrame.collection) {
                                if (prevNode != null) {
                                    yield(prevNode, RDF_REST, node);
                                }
                                yield(node, RDF_FIRST, item);
                                prevNode = node;
                                node = blankNode().id();
                            }
                            yield(prevNode, RDF_REST, RDF_NIL);
                        }
                    }
                }
                stack.pop();
            }
        }

        @Override
        public void characters(char[] chars, int start, int len) throws SAXException {
            if (literalLevel > 0) { // this isn't RDF; we're in an XMLLiteral
                XMLUtil.escape(xmlLiteral, chars, start, len);
            } else if (pcdata != null) { // we're in RDF, collecting an attribute value
                // accumulate char data
                for (int i = start; i < start + len; i++) {
                    pcdata.append(chars[i]);
                }
            }
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
            if (literalLevel > 0) {
                xmlLiteral.append("<?").append(target).append(" ").append(data).append("?>");
            }
        }
    }
}
