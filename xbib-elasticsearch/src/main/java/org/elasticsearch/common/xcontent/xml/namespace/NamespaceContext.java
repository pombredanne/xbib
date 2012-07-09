package org.elasticsearch.common.xcontent.xml.namespace;

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
 * A simple context for XML namespaces
 * 
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class NamespaceContext implements javax.xml.namespace.NamespaceContext {

    private static NamespaceContext instance;
    // we use TreeMap here for platform-independent stable order of name space definitions
    private final SortedMap<String, String> namespaces = new TreeMap<>();
    private final SortedMap<String, Set<String>> prefixes = new TreeMap<>();

    private NamespaceContext() {
    }

    private NamespaceContext(String bundleName) {
        this(ResourceBundle.getBundle(bundleName));
    }

    private NamespaceContext(ResourceBundle bundle) {
        Enumeration<String> en = bundle.getKeys();
        while (en.hasMoreElements()) {
            String prefix = en.nextElement();
            String namespace = bundle.getString(prefix);
            addNamespace(prefix, namespace);
        }
    }

    public static NamespaceContext getInstance() {
        if (instance == null) {
            instance = new NamespaceContext("org.elasticsearch.namespace");
        }
        return instance;
    }

    public static NamespaceContext newInstance() {
        return new NamespaceContext();
    }

    public static NamespaceContext newInstance(String bundleName) {
        return new NamespaceContext(bundleName);
    }

    public final synchronized void addNamespace(String prefix, String namespace) {
        namespaces.put(prefix, namespace);
        if (prefixes.containsKey(namespace)) {
            (prefixes.get(namespace)).add(prefix);
        } else {
            Set<String> set = new HashSet<>();
            set.add(prefix);
            prefixes.put(namespace, set);
        }
    }

    public final synchronized void removeNamespace(String prefix) {
        String ns = getNamespaceURI(prefix);
        if (ns != null) {
            prefixes.remove(ns);
        }
        namespaces.remove(prefix);
    }

    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    public boolean isPrefix(String prefix) {
        return getNamespaceURI(prefix) == null;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            return null;
        }
        return namespaces.containsKey(prefix) ? namespaces.get(prefix) : null;
    }

    @Override
    public String getPrefix(String namespaceURI) {
        Iterator<String> it = getPrefixes(namespaceURI);
        return it.hasNext() ? it.next() : null;
    }

    @Override
    public Iterator<String> getPrefixes(String namespace) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace URI cannot be null");
        }
        return prefixes.containsKey(namespace) ? prefixes.get(namespace).iterator() : Collections.EMPTY_SET.iterator();
    }

    /**
     * Abbreviate an URI with a full namespace URI to a short form URI with help of
     * the prefix in this namespace context.
     *
     * @param uri the long URI
     * @return a reduced short URI or the original URI if there is no prefix in
     * this context
     */
    public String abbreviate(URI uri) throws URISyntaxException {
        return abbreviate(uri, false);
    }

    public String abbreviate(URI uri, boolean dropfragment) throws URISyntaxException {
        // drop fragment (useful for resource counters in fragments)        
        final String s = dropfragment ? new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null).toString() : uri.toString();
        // we assume we have a rather short set of name spaces (~ 10-20)
        // otherwise, a binary search in an ordered key set would be more efficient.
        for (final String ns : prefixes.keySet()) {
            if (s.startsWith(ns)) {
                return new StringBuilder().append(getPrefix(ns)).append(':').append(s.substring(ns.length())).toString();
            }
        }
        return s;
    }

    public String getPrefix(URI uri) {
        return getNamespaceURI(uri.getScheme()) != null ? uri.getScheme() : getPrefix(uri.toString());
    }    
    
    public String[] isNamespace(URI uri) {
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

    public Set<String> getNamespacePrefixes() {
        return namespaces.keySet();
    }

    public void clear() {
        prefixes.clear();
        namespaces.clear();
    }
}
