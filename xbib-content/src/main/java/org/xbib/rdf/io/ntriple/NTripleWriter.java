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
import org.xbib.rdf.Identifier;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Node;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;

public class NTripleWriter<S extends Identifier, P extends Property, O extends Node> {

    private final static char LF = '\n';

    /**
     * Write to output stream
     *
     * @param resource
     * @param out
     * @throws IOException
     */
    public void write(Resource<S, P, O> resource, OutputStream out) throws IOException {
        write(resource, new OutputStreamWriter(out, Charset.forName("UTF-8")));
    }

    /**
     * @param resource
     * @param writer
     * @throws IOException
     */
    public void write(Resource<S, P, O> resource, Writer writer) throws IOException {
        StringBuilder sb = new StringBuilder();
        Iterator<Statement<S, P, O>> it = resource.iterator();
        while (it.hasNext()) {
            sb.append(writeStatement(it.next()));
        }
        writer.write(sb.toString());
    }

    public String writeStatement(Statement<S, P, O> stmt) throws IOException {
        S subj = stmt.subject();
        P pred = stmt.predicate();
        O obj = stmt.object();
        return new StringBuilder().append(writeSubject(subj)).append(" ").append(writePredicate(pred)).append(" ").append(writeObject(obj)).append(" .").append(LF).toString();
    }

    public String writeSubject(S subject) {
        return "<" + escape(subject.toString()) + ">" ;
    }

    public String writePredicate(P predicate) {
        return "<" + escape(predicate.toString()) + ">";
    }

    public String writeObject(O object) {
        if (object instanceof Resource) {
            S subject = ((Resource<S, P, O>) object).subject();
            return writeSubject(subject);
        } else if (object instanceof Literal) {
            Literal<?> value = (Literal<?>) object;
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
        }
        return "<???>";
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
