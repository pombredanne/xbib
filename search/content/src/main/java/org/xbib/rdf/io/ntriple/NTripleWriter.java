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
package org.xbib.rdf.io.ntriple;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Iterator;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.IdentifiableNode;
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Triple;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.io.ResourceSerializer;
import org.xbib.rdf.io.TripleListener;
import org.xbib.rdf.simple.SimpleResource;

/**
 * NTriple writer
 *
 */
public class NTripleWriter<S extends Identifier, P extends Property, O extends Node>
    implements ResourceSerializer<S,P,O>, TripleListener<S,P,O> {

    private final Logger logger = LoggerFactory.getLogger(NTripleWriter.class.getName());

    private final static char LF = '\n';

    /**
     * A Namespace context with URI-related methods
     */
    private IRINamespaceContext context;

    private IRI nullPredicate;

    private Writer writer;

    private Resource resource;

    private long byteCounter;

    private long idCounter;

    private String translatePicaSortMarker;

    public NTripleWriter() {
        this.context = IRINamespaceContext.newInstance();
        this.resource = new SimpleResource();
        this.byteCounter = 0L;
        this.idCounter = 0L;
        this.translatePicaSortMarker = null;
    }

    public long getByteCounter() {
        return byteCounter;
    }

    public long getIdentifierCounter() {
        return idCounter;
    }

    public NTripleWriter output(OutputStream out) {
        this.writer = new OutputStreamWriter(out, Charset.forName("UTF-8"));
        return this;
    }

    public NTripleWriter output(Writer writer) {
        this.writer = writer;
        return this;
    }

    public NTripleWriter setNullPredicate(IRI iri) {
        this.nullPredicate = iri;
        return this;
    }

    public NTripleWriter translatePicaSortMarker(String marker) {
        this.translatePicaSortMarker = marker;
        return this;
    }

    @Override
    public NTripleWriter newIdentifier(IRI iri) {
        if (!iri.equals(resource.id())) {
            try {
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
    public NTripleWriter triple(Triple triple) {
        resource.add(triple);
        return this;
    }


    @Override
    public NTripleWriter startPrefixMapping(String prefix, String uri) {
        context.addNamespace(prefix, uri);
        return this;
    }

    @Override
    public NTripleWriter endPrefixMapping(String prefix) {
        // we don't remove name spaces. It's troubling RDF serializations.
        //context.removeNamespace(prefix);
        return this;
    }

    public void close() {
        try {
            write(resource);
            idCounter++;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public NTripleWriter<S,P,O> write(Resource<S, P, O> resource) throws IOException {
        StringBuilder sb = new StringBuilder();
        Iterator<Triple<S,P,O>> it = resource.iterator();
        while (it.hasNext()) {
            sb.append(writeStatement(it.next()));
        }
        byteCounter += sb.length();
        writer.write(sb.toString());
        return this;
    }

    public String writeStatement(Triple<S, P, O> stmt) throws IOException {
        S subj = stmt.subject();
        P pred = stmt.predicate();
        O obj = stmt.object();
        return new StringBuilder().append(writeSubject(subj)).append(" ").append(writePredicate(pred)).append(" ").append(writeObject(obj)).append(" .").append(LF).toString();
    }

    public String writeSubject(S subject) {
        return //Identifier.GENID.equals(subject.id().getScheme()) ?
                subject.isBlank() ?
                subject.toString() :
                "<" + escape(subject.toString()) + ">" ;
    }

    public String writePredicate(P predicate) {
        if (predicate.id().getScheme() == null && nullPredicate !=null) {
            IRI iri = IRI.builder()
                    .scheme(nullPredicate.getScheme())
                    .host(nullPredicate.getHost())
                    .path(nullPredicate.getPath() + "/" + predicate.id().getSchemeSpecificPart())
                    .build();
            return "<" + escape(iri.toString()) + ">";
        }
        return "<" + escape(predicate.id().toString()) + ">";
    }

    public String writeObject(O object) {
        if (object instanceof Resource) {
            S subject = ((Resource<S, P, O>) object).subject();
            return writeSubject(subject);
        } else if (object instanceof Literal) {
            Literal<?> value = (Literal<?>) object;
            if (translatePicaSortMarker != null) {
                value = recognizeSortOrder(value, translatePicaSortMarker);
            }
            String s = "\"" + escape(value.object().toString()) + "\"";
            String lang = value.language();
            IRI type = value.type();
            if (lang != null) {
                return s + "@" + lang;
            }
            if (type != null) {
                return s + "^^<" + escape(type.toString()) + ">";
            }
            return s;
        } else if (object instanceof IdentifiableNode) {
            IdentifiableNode node = (IdentifiableNode)object;
            return //Identifier.GENID.equals(node.id().getScheme()) ?
                    node.isBlank() ?
                    node.toString() :
                    "<" + escape(node.toString()) + ">" ;
        }
        return "<???>";
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

    private String escape(String buffer) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buffer.length(); i++) {
            char ch = buffer.charAt(i);
            switch (ch) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (ch >= 32 && ch <= 126) {
                        sb.append(ch);
                    } else {
                        sb.append("\\u");
                        String hexstr = Integer.toHexString(ch).toUpperCase();
                        int pad = 4 - hexstr.length();
                        for (; pad > 0; pad--) {
                            sb.append("0");
                        }
                        sb.append(hexstr);
                    }
            }
        }
        return sb.toString();
    }
}
