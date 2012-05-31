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

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Contains a simple mapping between namespace prefixes and namespace URIs.
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class SimpleNamespaceContext
        implements NamespaceContext, Serializable {

    private static final long serialVersionUID = -1563876730477586046L;
    private static SimpleNamespaceContext instance;
    // use TreeMap here, for platform-independent stable sort of name space definitions
    private final SortedMap<String, String> urisByPrefix = new TreeMap<>();
    private final SortedMap<String, Set<String>> prefixesByURI = new TreeMap<>();
    private final static char DEFAULT_DELIMITER = ':';

    protected SimpleNamespaceContext() {
    }

    protected SimpleNamespaceContext(String bundleName) {
        this(ResourceBundle.getBundle(bundleName));
    }

    protected SimpleNamespaceContext(ResourceBundle bundle) {
        Enumeration<String> en = bundle.getKeys();
        while (en.hasMoreElements()) {
            String prefix = en.nextElement();
            String namespace = bundle.getString(prefix);
            addNamespace(prefix, namespace);
        }
    }

    public static SimpleNamespaceContext getInstance() {
        if (instance == null) {
            instance = new SimpleNamespaceContext("org.xbib.xml.namespace");
        }
        return instance;
    }

    public static SimpleNamespaceContext newInstance() {
        return new SimpleNamespaceContext();
    }

    public static SimpleNamespaceContext newInstance(String bundleName) {
        return new SimpleNamespaceContext(bundleName);
    }

    @Override
    public final synchronized void addNamespace(String prefix, String namespaceURI) {
        urisByPrefix.put(prefix, namespaceURI);
        if (prefixesByURI.containsKey(namespaceURI)) {
            (prefixesByURI.get(namespaceURI)).add(prefix);
        } else {
            Set<String> set = new HashSet<>();
            set.add(prefix);
            prefixesByURI.put(namespaceURI, set);
        }
    }

    public final synchronized void removeNamespace(String prefix) {
        String ns = getNamespaceURI(prefix);
        if (ns != null) {
            prefixesByURI.remove(ns);
        }
        urisByPrefix.remove(prefix);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            return null;
        }
        return urisByPrefix.containsKey(prefix) ? urisByPrefix.get(prefix) : null;
        // XMLConstants.NULL_NS_URI;
    }

    @Override
    public String getPrefix(String namespaceURI) {
        Iterator<String> it = getPrefixes(namespaceURI);
        return it.hasNext() ? it.next() : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("namespace URI cannot be null");
        }
        return prefixesByURI.containsKey(namespaceURI)
                ? prefixesByURI.get(namespaceURI).iterator() : Collections.EMPTY_SET.iterator();
    }

    public String getPrefix(URI uri) {
        return getNamespaceURI(uri.getScheme()) != null
                ? uri.getScheme() : getPrefix(uri.toString());
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
        return abbreviate(uri, DEFAULT_DELIMITER, false);
    }

    @Override
    public String abbreviate(URI uri, char delimiter, boolean dropfragment) throws URISyntaxException {
        // drop fragment (useful for resource counters in fragments)        
        final String s = dropfragment
                ? new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null).toString() : uri.toString();
        // we assume we have a rather short set of name spaces (~ 10-20)
        // otherwise, a binary search in an ordered key set would be more efficient.
        for (final String ns : prefixesByURI.keySet()) {
            if (s.startsWith(ns)) {
                return new StringBuilder().append(getPrefix(ns)).append(delimiter).append(s.substring(ns.length())).toString();
            }
        }
        return s;
    }

    @Override
    public String[] inContext(URI uri) {
        if (uri == null) {
            return null;
        }
        String scheme = uri.getScheme();
        if (scheme != null && urisByPrefix.containsKey(scheme)) {
            return new String[]{scheme, urisByPrefix.get(scheme)};
        }
        final String s = uri.toString();
        for (final String ns : prefixesByURI.keySet()) {
            if (s.startsWith(ns)) {
                return new String[]{getPrefix(ns), ns};
            }
        }
        return null;
    }

    public Set<String> getNamespacePrefixes() {
        return urisByPrefix.keySet();
    }

    @Override
    public Map<String, String> getNamespaceMap() {
        return urisByPrefix;
    }

    @Override
    public boolean checkForEmptyNamespace(String prefix) {
        return getNamespaceURI(prefix) == null; //.equals(XMLConstants.NULL_NS_URI);
    }

    public void clear() {
        prefixesByURI.clear();
        urisByPrefix.clear();
    }
}
