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
import java.util.StringTokenizer;
import org.xbib.iri.IRI;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.RDF;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.xml.CompactingNamespaceContext;

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
public class TurtleWriter<S extends Identifier, P extends Property, O extends Node> {

    private final static char LF = '\n';
    private final static char TAB = '\t';
    /**
     * Writer
     */
    private Writer writer;
    /**
     * A Namespace context with URI-related methods
     */
    private IRINamespaceContext context;
    /**
     * Flag for write start
     */
    private boolean writingStarted;
    /**
     * indicate whether a statement being written is in a resource or not
     */
    private boolean sameSubject;
    /*
     * The subject last visited
     */
    private S lastSubject;
    /**
     * The predicate last visited
     */
    private P lastPredicate;
    /**
     * for indenting
     */
    private Stack<S> subjects;
    private Stack<P> predicates;
    /**
     * the current statement
     */
    private Statement<S, P, O> statement;
    /**
     * counter for written triples
     */
    private long tripleCounter;

    public TurtleWriter() {
        this(IRINamespaceContext.getInstance());
    }

    public TurtleWriter(IRINamespaceContext context) {
        this.context = context;
    }

    /**
     *
     * @param resource
     * @param withPrefixes
     * @param out
     * @throws IOException
     */
    public void write(Resource<S, P, O> resource, boolean withPrefixes, OutputStream out) throws IOException {
        write(resource, withPrefixes, new OutputStreamWriter(out, "UTF-8"));
    }

    /**
     *
     * @param resource
     * @param withPrefixes
     * @param writer
     * @throws IOException
     */
    public void write(Resource<S, P, O> resource, boolean withPrefixes, Writer writer) throws IOException {
        if (resource == null) {
            throw new NullPointerException("resource is null");
        }
        this.writer = writer;
        this.writingStarted = false;
        this.sameSubject = false;
        this.lastSubject = null;
        this.lastPredicate = null;
        this.subjects = new Stack<>();
        this.predicates = new Stack<>();
        start();
        if (withPrefixes) {
            writeNamespaces(context, resource);
        }
        writeResource(resource);
        end();
    }

    public void writeNamespaces(CompactingNamespaceContext context) throws IOException {
        boolean written = false;
        for (Map.Entry<String, String> entry : context.getNamespaces().entrySet()) {
            if (entry.getValue().length() > 0) {
                String nsURI = entry.getValue().toString();
                if (!RDF.NS_URI.equals(nsURI)) {
                    writeNamespace(entry.getKey(), nsURI);
                    written = true;
                }
            }
        }
        if (written) {
            writer.write(LF);
        }
    }

    private void writeNamespaces(IRINamespaceContext context, Resource<S, P, O> resource) throws IOException {
        // first, collect namespace URIs and prefixes
        IRINamespaceContext newContext = IRINamespaceContext.newInstance();
        Iterator<Statement<S, P, O>> it = resource.iterator();
        while (it.hasNext()) {
            Statement<S, P, O> stmt = it.next();
            String[] s = context.inContext(stmt.subject().id());
            if (s != null) {
                newContext.addNamespace(s[0], s[1]);
            }
            s = context.inContext(stmt.predicate().id());
            if (s != null) {
                newContext.addNamespace(s[0], s[1]);
            }
            O o = stmt.object();
            if (o instanceof Resource) {
                s = context.inContext(((Resource) o).id());
                if (s != null) {
                    newContext.addNamespace(s[0], s[1]);
                }
            } else if (o instanceof Literal) {
                s = context.inContext(((Literal) o).type());
                if (s != null) {
                    newContext.addNamespace(s[0], s[1]);
                }
            }
        }
        // second, write namespaces in use
        writeNamespaces(newContext);
        this.context = newContext;
    }

    private void writeResource(Resource<S, P, O> resource) throws IOException {
        Iterator<Statement<S, P, O>> it = resource.iterator();
        while (it.hasNext()) {
            Statement<S, P, O> stmt = it.next();
            handleStatement(stmt);
            tripleCounter++;
        }
    }

    public long getTripleCounter() {
        return tripleCounter;
    }

    public void start() throws IOException {
        if (writingStarted) {
            throw new IOException("document writing has already started");
        }
        writingStarted = true;
    }

    public void end() throws IOException {
        if (!writingStarted) {
            throw new IOException("document writing has not yet started");
        }
        try {
            if (sameSubject) {
                closeResource();
                writer.write('.');
            }
        } finally {
            writer.flush();
            writingStarted = false;
        }
    }

    /**
     * Serialize namespace declaration
     *
     * @param prefix
     * @param name
     * @throws IOException
     */
    public void handleNamespace(String prefix, String name) throws IOException {
        if (context.getPrefix(name) == null) {
            // Namespace not yet mapped to a prefix, try to give it the
            // specified prefix
            boolean isLegalPrefix = prefix.length() == 0 || isLegalPrefix(prefix);
            if (!isLegalPrefix || context.getNamespaceURI(prefix) != null) {
                // Specified prefix is not legal or the prefix is already in use,
                // generate a legal unique prefix
                if (prefix.length() == 0 || !isLegalPrefix) {
                    prefix = Identifier.BLANK_PREFIX;
                }
                int number = 1;
                while (context.getNamespaceURI(prefix + number) != null) {
                    number++;
                }
                prefix += number;
            }
            context.addNamespace(prefix, name);
            if (writingStarted) {
                closeSubject();
                writeNamespace(prefix, name);
                writer.write(LF);
            }
        }
    }

    /**
     * Serialize statement
     *
     * @param stmt
     * @throws IOException
     */
    public void handleStatement(Statement<S, P, O> stmt) throws IOException {
        if (!writingStarted) {
            throw new IOException("document writing has not yet been started");
        }
        this.statement = stmt;
        S subj = stmt.subject();
        P pred = stmt.predicate();
        O obj = stmt.object();
        if (subj.equals(lastSubject)) {
            if (pred.equals(lastPredicate)) {
                // continous object enumeration if same subject and predicate
                writer.write(", ");
            } else {
                // same subject, new predicate
                writer.write(";");
                writer.write(LF);
                writeIndent(subjects.size() + 1);
                writePredicate(pred);
            }
        } else {
            // another subject or blank node, let's indent
            closeSubject();
            writeIndent(subjects.size());
            if (!sameSubject) {
                writeSubject(subj);
                sameSubject = true;
            }
            writePredicate(pred);
        }
        writeObject(obj);
    }

    /**
     * Serialize comment
     *
     * @param comment
     * @throws IOException
     */
    public void handleComment(String comment) throws IOException {
        closeSubject();
        if (comment.indexOf('\r') != -1 || comment.indexOf('\n') != -1) {
            // Comment is not allowed to contain newlines or line feeds.
            // Split comment in individual lines and write comment lines
            // for each of them.
            StringTokenizer st = new StringTokenizer(comment, "\r\n");
            while (st.hasMoreTokens()) {
                writeCommentLine(st.nextToken());
            }
        } else {
            writeCommentLine(comment);
        }
    }

    private void writeCommentLine(String line) throws IOException {
        writer.write("# ");
        writer.write(line);
        writer.write(LF);
    }

    private void writeNamespace(String prefix, String name) throws IOException {
        writer.write("@prefix ");
        writer.write(prefix);
        writer.write(": <");
        writer.write(encodeURIString(name));
        writer.write("> .");
        writer.write(LF);
    }

    private void writeSubject(S subject) throws IOException {
        if (subject == null || subject.id() == null) {
            return;
        }
        // skip blank nodes 
        if (!Identifier.BLANK_PREFIX.equals(subject.id().getScheme())) {
            writer.write('<');
            writer.write(subject.toString());
            writer.write("> ");
        }
        lastSubject = subject;
    }

    private void writePredicate(P predicate) throws IOException {
        if ("rdf:type".equals(predicate.toString()) || predicate.toString().equals(RDF.NS_URI + "type")) {
            writer.write("a ");
        } else {
            writeURI(predicate.id());
            writer.write(" ");
        }
        lastPredicate = predicate;
    }

    // SuppressWarnings("unchecked")
    private void writeObject(O object) throws IOException {
        if (object instanceof Identifier) {
            sameSubject = false;
            openResource((Identifier) object);
        } else if (object instanceof Resource) {
            writeURI(((Resource<S, P, O>) object).id());
        } else if (object instanceof Literal) {
            writeLiteral((Literal<?>) object);
        } else {
            throw new IllegalArgumentException("unknown value class: " + (object != null ? object.getClass() : "<null>"));
        }
    }

    private void openResource(Identifier node) throws IOException {
        writer.write("[");
        writer.write(LF);
        writeIndent(1);
        subjects.push(statement.subject());
        predicates.push(statement.predicate());
    }

    private void closeResource() throws IOException {
        if (!subjects.isEmpty()) {
            writer.write(LF);
            writeIndent(subjects.size());
            writer.write("]");
            lastSubject = subjects.pop();
            lastPredicate = predicates.pop();
        } else {
            lastSubject = null;
            lastPredicate = null;
        }
    }

    private void writeURI(IRI uri) throws IOException {
        String abbrev = context.compact(uri, ':', false);
        if (!abbrev.equals(uri.toString())) {
            writer.write(abbrev);
            return;
        }
        if (context.getNamespaceURI(uri.getScheme()) != null) {
            writer.write(uri.toString());
            return;
        }
        // Write full URI
        writer.write("<");
        writer.write(encodeURIString(uri.toString()));
        writer.write(">");
    }

    private void writeLiteral(Literal literal) throws IOException {
        String value = literal.object().toString();
        if (value.indexOf('\n') > 0 || value.indexOf('\r') > 0 || value.indexOf('\t') > 0) {
            // Write label as long string
            writer.write("\"\"\"");
            writer.write(encodeLongString(value));
            writer.write("\"\"\"");
        } else {
            // Write label as normal string
            writer.write("\"");
            writer.write(encodeString(value));
            writer.write("\"");
        }
        if (literal.type() != null) {
            // Append the literal's type
            writer.write("^^");
            writeURI(literal.type());
        } else if (literal.language() != null) {
            // Append the literal's language
            writer.write("@");
            writer.write(literal.language());
        }
    }

    private void closeSubject() throws IOException {
        if (sameSubject) {
            closeResource();
            if (statement.subject().equals(lastSubject)) {
                writer.write(";");
                writer.write(LF);
                writeIndent(1); // ?
            } else {
                writer.write(".");
                writer.write(LF);
                sameSubject = false;
            }
        }
    }

    private void writeIndent(int indentLevel) throws IOException {
        for (int i = 0; i < indentLevel; i++) {
            writer.write(TAB);
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
}
