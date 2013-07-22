/**
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
package org.xbib.rdf.context;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.xbib.common.xcontent.xml.XmlNamespaceContext;
import org.xbib.iri.IRI;
import org.xbib.iri.CompactingNamespaceContext;

public final class IRINamespaceContext extends XmlNamespaceContext implements CompactingNamespaceContext {

    private static IRINamespaceContext instance;

    private IRINamespaceContext() {
    }

    private IRINamespaceContext(ResourceBundle bundle) {
        Enumeration<String> en = bundle.getKeys();
        while (en.hasMoreElements()) {
            String prefix = en.nextElement();
            String namespace = bundle.getString(prefix);
            addNamespace(prefix, namespace);
        }
    }

    public static IRINamespaceContext getInstance() {
        if (instance == null) {
            try {
                instance = new IRINamespaceContext(ResourceBundle.getBundle("org.xbib.xml.namespace"));
            } catch (MissingResourceException e) {
                instance = new IRINamespaceContext();
            }
        }
        return instance;
    }

    public static IRINamespaceContext newInstance() {
        return new IRINamespaceContext();
    }
    /**
     * Abbreviate an URI with a full namespace URI to a short form URI with help of
     * the prefix in this namespace context.
     *
     * @param uri the long URI
     * @return a compact short URI or the original URI if there is no prefix in
     * this context
     */
    @Override
    public String compact(IRI uri) {
        return compact(uri, false);
    }

    public String compact(IRI uri, boolean dropfragment) {
        // drop fragment (useful for resource counters in fragments)
        final String s = dropfragment
                ? new IRI(uri.getScheme(), uri.getSchemeSpecificPart(), null).toString() : uri.toString();
        // we assume we have a rather short set of name spaces (~ 10-20)
        // otherwise, a binary search in an ordered key set would be more efficient.
        for (final String ns : getNamespaces().values()) {
            if (s.startsWith(ns)) {
                return new StringBuilder().append(getPrefix(ns)).append(':').append(s.substring(ns.length())).toString();
            }
        }
        return s;
    }

    public String getPrefix(IRI uri) {
        return getNamespaceURI(uri.getScheme()) != null ? uri.getScheme() : getPrefix(uri.toString());
    }
}
