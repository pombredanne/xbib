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
package org.xbib.rdf.simple;

import com.google.common.base.Objects;
import java.net.URI;
import org.xbib.rdf.Literal;

/**
 *  A simple Literal is a value of object type
 *
 *  @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class SimpleLiteral<O extends Object>
        implements Literal<O>, Comparable<Literal<O>> {

    private O value;
    private URI type;
    private String lang;

    public SimpleLiteral() {
    }

    public SimpleLiteral(O value) {
        this.value = value;
    }

    public SimpleLiteral(O value, String lang) {
        this.value = value;
        this.lang = lang;
    }

    public SimpleLiteral(O value, URI type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public void setValue(O value) {
        this.value = value;
    }


    @Override
    public O getValue() {
        return value;
    }

    public void setType(URI type) {
        this.type = type;
    }
    
    @Override
    public URI getType() {
        return type;
    }

    public void setLanguage(String lang) {
        this.lang = lang;
    }
    
    @Override
    public String getLanguage() {
        return lang;
    }
    
    @Override
    public int compareTo(Literal<O> that) {
        if (this == that) {
            return 0;
        }
        if (that == null) {
            return 1;
        }
        return toString().compareTo(that.toString());
    }

    @Override
    public boolean equals(Object obj) {        
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Literal)) {
            return false;
        }
        final Literal that = (Literal)obj;
        return Objects.equal(this.value, that.getValue())
                && Objects.equal(this.lang, that.getLanguage())
                && Objects.equal(this.type, that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value, lang, type);
    }

    @Override
    public String toString() {
        return (value != null ? value : "")
                + (lang != null ? "@" + lang : "")
                + (type != null ? "^^<" + type + ">" : "");
    }

}
