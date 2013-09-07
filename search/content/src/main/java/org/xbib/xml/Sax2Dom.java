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
package org.xbib.xml;

import java.util.ArrayList;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class Sax2Dom implements XMLNS, ContentHandler, LexicalHandler {

    private static final String XMLNS_PREFIX = NS_PREFIX;

    private static final String XMLNS_STRING = NS_PREFIX + ":";

    private static final String XMLNS_URI = NS_URI;

    private final Node root;

    private Document document = null;

    private Node nextSibling = null;

    private Stack<Node> stack = new Stack();

    private ArrayList namespaceDecls = null;

    private Node lastSibling = null;

    public Sax2Dom() throws ParserConfigurationException {
        final DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        document = factory.newDocumentBuilder().newDocument();
        root = document;
        // disable <,>,& escaping 
        ProcessingInstruction pi =
                document.createProcessingInstruction(Result.PI_DISABLE_OUTPUT_ESCAPING, "");
        root.appendChild(pi);
    }

    public Node getDOM() {
        return root;
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        final Node last = stack.peek();
        // No text nodes can be children of root (DOM006 exception)
        if (last != document) {
            String text = new String(ch, start, length);
            // do our escaping 
            text = text.replaceAll("&","&amp;").replaceAll("<", "&lt;");
            if (lastSibling != null && lastSibling.getNodeType() == Node.TEXT_NODE) {
                ((Text) lastSibling).appendData(text);
            } else if (last == root && nextSibling != null) {
                lastSibling = last.insertBefore(document.createTextNode(text), nextSibling);
            } else {
                lastSibling = last.appendChild(document.createTextNode(text));
            }
        }
    }

    @Override
    public void startDocument() {
        stack.push(root);
    }

    @Override
    public void endDocument() {
        stack.pop();
    }

    @Override
    public void startElement(String namespace, String localName, String qName,
            Attributes attrs) {
        final Element tmp = document.createElementNS(namespace, qName);

        // Add namespace declarations first
        if (namespaceDecls != null) {
            final int nDecls = namespaceDecls.size();
            for (int i = 0; i < nDecls; i++) {
                final String prefix = (String) namespaceDecls.get(i++);

                if (prefix == null || prefix.equals("")) {
                    tmp.setAttributeNS(XMLNS_URI, XMLNS_PREFIX,
                            (String) namespaceDecls.get(i));
                } else {
                    tmp.setAttributeNS(XMLNS_URI, XMLNS_STRING + prefix,
                            (String) namespaceDecls.get(i));
                }
            }
            namespaceDecls.clear();
        }

        // Add attributes to element
        final int nattrs = attrs.getLength();
        for (int i = 0; i < nattrs; i++) {
            if (attrs.getLocalName(i) == null) {
                tmp.setAttribute(attrs.getQName(i), attrs.getValue(i));
            } else {
                tmp.setAttributeNS(attrs.getURI(i), attrs.getQName(i),
                        attrs.getValue(i));
            }
        }
        Node last = stack.peek();
        if (last == root && nextSibling != null) {
            last.insertBefore(tmp, nextSibling);
        } else {
            last.appendChild(tmp);
        }
        stack.push(tmp);
        lastSibling = null;
    }

    @Override
    public void endElement(String namespace, String localName, String qName) {
        stack.pop();
        lastSibling = null;
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) {
        if (namespaceDecls == null) {
            namespaceDecls = new ArrayList(2);
        }
        namespaceDecls.add(prefix);
        namespaceDecls.add(uri);
    }

    @Override
    public void endPrefixMapping(String prefix) {
        // do nothing
    }

    /**
     * This class is only used internally so this method should never 
     * be called.
     */
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) {
    }

    /**
     * adds processing instruction node to DOM.
     */
    @Override
    public void processingInstruction(String target, String data) {
        final Node last = stack.peek();
        ProcessingInstruction pi = document.createProcessingInstruction(
                target, data);
        if (pi != null) {
            if (last == root && nextSibling != null) {
                last.insertBefore(pi, nextSibling);
            } else {
                last.appendChild(pi);
            }
            lastSibling = pi;
        }
    }

    /**
     * This class is only used internally so this method should never 
     * be called.
     */
    @Override
    public void setDocumentLocator(Locator locator) {
    }

    /**
     * This class is only used internally so this method should never 
     * be called.
     */
    @Override
    public void skippedEntity(String name) {
    }

    /**
     * Lexical Handler method to create comment node in DOM tree.
     */
    @Override
    public void comment(char[] ch, int start, int length) {
        final Node last = stack.peek();
        Comment comment = document.createComment(new String(ch, start, length));
        if (comment != null) {
            if (last == root && nextSibling != null) {
                last.insertBefore(comment, nextSibling);
            } else {
                last.appendChild(comment);
            }
            lastSibling = comment;
        }
    }

    // Lexical Handler methods- not implemented
    @Override
    public void startCDATA() {
    }

    @Override
    public void endCDATA() {
    }

    @Override
    public void startEntity(java.lang.String name) {
    }

    @Override
    public void endDTD() {
    }

    @Override
    public void endEntity(String name) {
    }

    @Override
    public void startDTD(String name, String publicId, String systemId)
            throws SAXException {
    }
}