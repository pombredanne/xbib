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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.ResourceBundle;

public final class URINamespaceContext extends SimpleNamespaceContext {

    private static URINamespaceContext instance;

    private URINamespaceContext() {
    }

    private URINamespaceContext(String bundleName) {
        this(ResourceBundle.getBundle(bundleName));
    }

    private URINamespaceContext(ResourceBundle bundle) {
        Enumeration<String> en = bundle.getKeys();
        while (en.hasMoreElements()) {
            String prefix = en.nextElement();
            String namespace = bundle.getString(prefix);
            addNamespace(prefix, namespace);
        }
    }

    public static URINamespaceContext getInstance() {
        if (instance == null) {
            instance = new URINamespaceContext("org.xbib.xml.namespace");
        }
        return instance;
    }

    public static URINamespaceContext newInstance() {
        return new URINamespaceContext();
    }

    public static URINamespaceContext newInstance(String bundleName) {
        return new URINamespaceContext(bundleName);
    }    
    /**
     * Reduce an URI with a full namespace URI to a short form URI with help of
     * the prefix in this namespace context.
     *
     * @param uri the long URI
     * @return a reduced short URI or the original URI if there is no prefix in
     * this context
     */
    @Override
    public String abbreviate(URI uri) throws URISyntaxException {
        return abbreviate(uri, ':', false);
    }

    public String abbreviate(URI uri, char delimiter, boolean dropfragment) throws URISyntaxException {
        // drop fragment (useful for resource counters in fragments)        
        final String s = dropfragment
                ? new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null).toString() : uri.toString();
        // we assume we have a rather short set of name spaces (~ 10-20)
        // otherwise, a binary search in an ordered key set would be more efficient.
        for (final String ns : prefixes.keySet()) {
            if (s.startsWith(ns)) {
                return new StringBuilder().append(getPrefix(ns)).append(delimiter).append(s.substring(ns.length())).toString();
            }
        }
        return s;
    }

    public String[] inContext(URI uri) {
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        if (scheme != null && namespaces.containsKey(scheme)) {
            return new String[]{scheme, namespaces.get(scheme)};
        }
        final String s = uri.toString();
        for (final String ns : prefixes.keySet()) {
            if (s.startsWith(ns)) {
                return new String[]{getPrefix(ns), ns};
            }
        }
        return null;
    }

}
