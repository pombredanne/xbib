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
package org.xbib.rdf.io.turtle;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.IdentifiableNode;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.RDF;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.io.RDFSerializer;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.simple.SimpleResource;

/**
 * RDF Turtle serialization
 *
 * See <a href="http://www.w3.org/TeamSubmission/turtle/">Turtle - Terse RDF
 * Triple Language</a>
 *
 * Warning, many bugs ahead.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class TurtleWriter<S extends Identifier, P extends Property, O extends Node>
    implements RDFSerializer<S,P,O>, TripleListener<S,P,O> {

    private final Logger logger = LoggerFactory.getLogger(TurtleWriter.class.getName());

    private final static char LF = '\n';

    private final static char TAB = '\t';

    private Writer writer;

    private Resource resource;
    /**
     * A Namespace context with URI-related methods
     */
    private IRINamespaceContext context;

    /**
     * Flag for write start
     */
    private boolean writingStarted;

    private boolean sameSubject;

    private boolean samePredicate;
    /**
     * for indenting
     */
    private Stack<Triple<S,P,O>> stack;

    private S lastSubject;

    private P lastPredicate;
    /**
     * the current triple
     */
    private Triple<S,P,O> triple;

    private boolean nsWritten;

    private String translatePicaSortMarker;

    private StringBuilder sb;

    private long byteCounter;

    private long idCounter;

    public TurtleWriter() {
        this.context = IRINamespaceContext.newInstance();
        this.nsWritten = false;
        this.resource = new SimpleResource();
        this.writingStarted = false;
        this.sameSubject = false;
        this.samePredicate = false;
        this.stack = new Stack();
        this.byteCounter = 0L;
        this.idCounter = 0L;
        this.sb = new StringBuilder();
        this.translatePicaSortMarker = null;
    }

    public TurtleWriter setContext(IRINamespaceContext context) {
        this.context = context;
        return this;
    }

    public long getByteCounter() {
        return byteCounter;
    }

    public long getIdentifierCounter() {
        return idCounter;
    }

    public TurtleWriter translatePicaSortMarker(String marker) {
        this.translatePicaSortMarker = marker;
        return this;
    }

    @Override
    public TurtleWriter newIdentifier(IRI iri) {
        if (!iri.equals(resource.id())) {
            try {
                if (!nsWritten) {
                    writeNamespaces();
                }
                write(resource);
                idCounter++;
                resource = new SimpleResource();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        resource.id(iri);
        return this;
    }

    @Override
    public TurtleWriter triple(Triple triple) {
        resource.add(triple);
        return this;
    }

    @Override
    public TurtleWriter startPrefixMapping(String prefix, String uri) {
        context.addNamespace(prefix, uri);
        return this;
    }

    @Override
    public TurtleWriter endPrefixMapping(String prefix) {
        // nooooo keep it!
        //context.removeNamespace(prefix);
        return this;
    }

    public void close() {
        try {
            write(resource);
            idCounter++;
            writer.flush();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public TurtleWriter output(OutputStream out) throws IOException {
        if (out == null) {
            return this;
        }
        this.writer = new OutputStreamWriter(out, "UTF-8");
        return this;
    }

    public TurtleWriter output(Writer writer) throws IOException {
        this.writer = writer;
        return this;
    }

    @Override
    public TurtleWriter write(Resource<S, P, O> resource) throws IOException {
        if (resource != null) {
            start();
            Iterator<Triple<S, P, O>> it = resource.iterator();
            while (it.hasNext()) {
                writeTriple(it.next());
            }
            end();
            if (writer != null) {
                writer.write(sb.toString());
            }
            byteCounter += sb.length();
            sb.setLength(0);
        }
        // for safe next resource writing, empty stack here
        stack.clear();
        return this;
    }

    private void start() throws IOException {
        if (writingStarted) {
            throw new IOException("document writing has already started");
        }
        writingStarted = true;
    }

    private void end() throws IOException {
        if (!writingStarted) {
            throw new IOException("document writing has not yet started");
        }
        try {
            closeStatement();
        } finally {
            writingStarted = false;
        }
    }

    public TurtleWriter writeNamespaces() throws IOException {
        if (context == null) {
            return this;
        }
        nsWritten = false;
        for (Map.Entry<String, String> entry : context.getNamespaces().entrySet()) {
            if (entry.getValue().length() > 0) {
                String nsURI = entry.getValue().toString();
                if (!RDF.NS_URI.equals(nsURI)) {
                    writeNamespace(entry.getKey(), nsURI);
                    nsWritten = true;
                }
            }
        }
        if (nsWritten) {
            sb.append(LF);
        }
        return this;
    }

    private void writeNamespace(String prefix, String name) throws IOException {
        sb.append("@prefix ")
                .append(prefix)
                .append(": <")
                .append(encodeURIString(name))
                .append("> .")
                .append(LF);
    }

    /**
     * Write triple
     *
     * @param stmt
     * @throws IOException
     */
    public void writeTriple(Triple<S,P,O> stmt) throws IOException {
        if (!writingStarted) {
            throw new IOException("document writing has not yet been started");
        }
        this.triple = stmt;
        S subject = stmt.subject();
        P predicate = stmt.predicate();
        O object = stmt.object();
        if (subject == null || predicate == null) {
            return;
        }
        if (subject.equals(lastSubject)) {
            if (predicate.equals(lastPredicate)) {
                sb.append(", ");
                writeObject(object);
            } else {
                // same subject, predicate changed
                sb.append(';');
                sb.append(LF);
                writeIndent(stack.size() + 1);
                writePredicate(predicate);
                writeObject(object);
            }
        } else {
            // another subject
            closeStatement();
            writeIndent(stack.size());
            if (!sameSubject) {
                writeSubject(subject);
                writePredicate(predicate);
                sameSubject = true;
                samePredicate = true;
            } else if (!samePredicate) {
                writePredicate(predicate);
                samePredicate = true;
            }
            writeObject(object);
        }
    }

    private void writeSubject(S subject) throws IOException {
        if (subject == null || subject.id() == null) {
            return;
        }
        // skip blank nodes 
        if (!isBlank(subject)) {
            sb.append('<')
                .append(subject.toString())
                .append("> ");
        }
        lastSubject = subject;
    }

    private void writePredicate(P predicate) throws IOException {
        if (predicate == null) {
            sb.append("<> ");
            return;
        }
        if ("rdf:type".equals(predicate.toString()) || predicate.toString().equals(RDF.NS_URI + "type")) {
            sb.append("a ");
        } else {
            writeURI(predicate.id());
            sb.append(" ");
        }
        lastPredicate = predicate;
    }

    private void writeObject(O object) throws IOException {
        if (object instanceof Resource) {
            Resource r = (Resource<S,P,O>)object;
            if (isBlank(r)) {
                // blank node?
                openResource();
                sameSubject = false;
                samePredicate = false;
                lastSubject = (S)r;
            } else {
                writeURI(r.id());
            }
        } else if (object instanceof Literal) {
            writeLiteral((Literal<?>) object);
        } else if (object instanceof IdentifiableNode) {
            writeURI(((IdentifiableNode)object).id());
        } else {
            throw new IllegalArgumentException("unknown value class: "
                    + (object != null ? object.getClass() : "<null>"));
        }
    }

    private void openResource() throws IOException {
        stack.push(triple);
        sb.append('[')
           .append(LF);
        writeIndent(1);
    }

    private void closeResource() throws IOException {
        if (!stack.isEmpty()) {
            sb.append(LF);
            writeIndent(stack.size());
            sb.append(']');
            Triple<S,P,O> t = stack.pop();
            lastSubject = t.subject();
            lastPredicate = t.predicate();
        } else {
            lastSubject = null;
            lastPredicate = null;
        }
    }

    private void writeURI(IRI uri) throws IOException {
        String abbrev = context.compact(uri);
        if (!abbrev.equals(uri.toString())) {
            sb.append(abbrev);
            return;
        }
        // URI scheme = namespace prefix in context?
        if (context.getNamespaceURI(uri.getScheme()) != null) {
            sb.append(uri.toString());
            return;
        }
        // Write full URI
        sb.append('<')
            .append(encodeURIString(uri.toString()))
            .append('>');
    }

    private void writeLiteral(Literal literal) throws IOException {
        if (translatePicaSortMarker != null) {
            literal = recognizeSortOrder(literal, translatePicaSortMarker);
        }
        String value = literal.nativeValue().toString();
        if (value.indexOf('\n') > 0 || value.indexOf('\r') > 0 || value.indexOf('\t') > 0) {
            // Write label as long string
            sb.append("\"\"\"")
            .append(encodeLongString(value))
            .append("\"\"\"");
        } else {
            // Write label as normal string
            sb.append('\"')
                .append(encodeString(value))
                .append('\"');
        }
        if (literal.type() != null) {
            // Append the literal's type
            sb.append("^^").append(literal.type().toString());
        } else if (literal.language() != null) {
            // Append the literal's language
            sb.append('@').append(literal.language());
        }
    }

    /**
     * see http://www.w3.org/International/articles/language-tags/
     *
     * @param literal
     * @param language
     * @return
     */
    private Literal recognizeSortOrder(Literal literal, String language) {
        String value = literal.object().toString();
        if (value.indexOf('@') == 0) {
            literal.object(value.substring(1));
        }
        int pos = value.indexOf(" @"); // PICA mechanical word order marker
        if (pos > 0) {
            literal.object('\u0098' + value.substring(0,pos+1) + '\u009c' + value.substring(pos+2))
                    .language(language);
        }
        return literal;
    }

    private void closeStatement() throws IOException {
        if (sameSubject) {
            closeResource();
            if (triple.subject().equals(lastSubject)) {
                if (triple.predicate().equals(lastPredicate)) {
                    sb.append(',');
                } else {
                    sb.append(';')
                    .append(LF);
                    writeIndent(1);
                    samePredicate = false;
                }
            } else {
                // what about stack???
                sb.append(".")
                .append(LF);
                sameSubject = false;
            }
        }
    }

    private void writeIndent(int indentLevel) throws IOException {
        for (int i = 0; i < indentLevel; i++) {
            sb.append(TAB);
        }
    }

    private boolean isPrefixStartChar(int c) {
        return Character.isLetter(c) || c >= 0x00C0 && c <= 0x00D6 || c >= 0x00D8 && c <= 0x00F6 || c >= 0x00F8 && c <= 0x02FF || c >= 0x0370 && c <= 0x037D || c >= 0x037F && c <= 0x1FFF || c >= 0x200C && c <= 0x200D || c >= 0x2070 && c <= 0x218F || c >= 0x2C00 && c <= 0x2FEF || c >= 0x3001 && c <= 0xD7FF || c >= 0xF900 && c <= 0xFDCF || c >= 0xFDF0 && c <= 0xFFFD || c >= 0x10000 && c <= 0xEFFFF;
    }

    private boolean isNameStartChar(int c) {
        return c == '_' || isPrefixStartChar(c);
    }

    private boolean isNameChar(int c) {
        return isNameStartChar(c) || Character.isDigit(c) || c == '-' || c == 0x00B7 || c >= 0x0300 && c <= 0x036F || c >= 0x203F && c <= 0x2040;
    }

    private boolean isPrefixChar(int c) {
        return isNameChar(c);
    }

    private boolean isLegalPrefix(String prefix) {
        if (prefix.length() == 0) {
            return false;
        }
        if (!isPrefixStartChar(prefix.charAt(0))) {
            return false;
        }
        for (int i = 1; i < prefix.length(); i++) {
            if (!isPrefixChar(prefix.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Encodes the supplied string for inclusion as a 'normal' string in a
     * Turtle document.
     */
    private String encodeString(String s) {
        s = gsub("\\", "\\\\", s);
        s = gsub("\t", "\\t", s);
        s = gsub("\n", "\\n", s);
        s = gsub("\r", "\\r", s);
        s = gsub("\"", "\\\"", s);
        return s;
    }

    /**
     * Encodes the supplied string for inclusion as a long string in a Turtle
     * document.
     *
     */
    private String encodeLongString(String s) {
        // not all double quotes need to be escaped. It suffices to encode
        // the ones that form sequences of 3 or more double quotes, and the ones
        // at the end of a string.
        s = gsub("\\", "\\\\", s);
        s = gsub("\"", "\\\"", s);
        return s;
    }

    /**
     * Encodes the supplied string for inclusion as a (relative) URI in a Turtle
     * document.
     *
     */
    private String encodeURIString(String s) {
        s = gsub("\\", "\\\\", s);
        s = gsub(">", "\\>", s);
        return s;
    }

    /**
     * Substitute String "old" by String "new" in String "text" everywhere. This
     * is static util function that I could not place anywhere more appropriate.
     * The name of this function is from the good-old awk time.
     *
     * @param olds The String to be substituted.
     * @param news The String is the new content.
     * @param text The String in which the substitution is done.
     * @return The result String containing the substitutions; if no
     * substitutions were made, the result is 'text'.
     */
    private String gsub(String olds, String news, String text) {
        if (olds == null || olds.length() == 0) {
            // Nothing to substitute.
            return text;
        }
        if (text == null) {
            return null;
        }
        // Search for any occurences of 'olds'.
        int oldsIndex = text.indexOf(olds);
        if (oldsIndex == -1) {
            // Nothing to substitute.
            return text;
        }
        // We're going to do some substitutions.
        StringBuilder buf = new StringBuilder(text.length());
        int prevIndex = 0;
        while (oldsIndex >= 0) {
            // First, add the text between the previous and the current
            // occurence.
            buf.append(text.substring(prevIndex, oldsIndex));
            // Then add the substition pattern
            buf.append(news);
            // Remember the index for the next loop.
            prevIndex = oldsIndex + olds.length();
            // Search for the next occurence.
            oldsIndex = text.indexOf(olds, prevIndex);
        }
        // Add the part after the last occurence.
        buf.append(text.substring(prevIndex));
        return buf.toString();
    }

    private boolean isBlank(Identifier n) {
        return Identifier.GENID.equals(n.id().getScheme());
    }
}
