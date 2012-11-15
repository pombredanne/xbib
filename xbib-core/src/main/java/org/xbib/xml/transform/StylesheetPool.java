/*
 * Carrot2 project.
 *
 * Copyright (C) 2002-2008, Dawid Weiss, Stanisław Osiński.
 * Portions (C) Contributors listed in "carrot2.CONTRIBUTORS" file.
 * All rights reserved.
 *
 * Refer to the full license file "carrot2.LICENSE"
 * in the root folder of the repository checkout or at:
 * http://www.carrot2.org/carrot2.LICENSE
 */
package org.xbib.xml.transform;

import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

/**
 * A pool of precompiled XSLT stylesheets ({@link Templates}).
 */
public final class StylesheetPool {

    /**
     * A map of precompiled stylesheets ({@link Templates} objects).
     */
    private volatile Map<String, Templates> stylesheets = new HashMap<>();

    /**
     * @return returns the identity transformer handler.
     */
    public TransformerHandler getIdentityTransformerHandler(SAXTransformerFactory transformerFactory)
            throws TransformerConfigurationException {
        return transformerFactory.newTransformerHandler();
    }

    public boolean hasTemplate(StreamSource source) {
        return stylesheets.containsKey(source.getSystemId());
    }

    /**
     * Retrieves a previously stored template, if available.
     */
    public Templates getTemplate(String systemId) {
        return stylesheets.get(systemId);
    }

    /**
     * Create a template, add to the pool if necessary. Addition is quite costly
     * as it replaces the internal {@link #stylesheets} {@link HashMap}.
     */
    public Templates newTemplates(SAXTransformerFactory transformerFactory, Source source) throws TransformerConfigurationException {
        String systemId = source.getSystemId();
        Templates template = stylesheets.get(systemId);
        if (template == null) {
            template = transformerFactory.newTemplates(source);
            final HashMap<String, Templates> newMap = new HashMap();
            newMap.put(systemId, template);
            stylesheets = newMap;
        }
        return template;
    }

    /**
     * Return a new {@link TransformerHandler} based on a given precompiled
     * {@link Templates}.
     */
    public TransformerHandler newTransformerHandler(SAXTransformerFactory transformerFactory, Templates template)
            throws TransformerConfigurationException {
        final TransformerHandler handler = transformerFactory.newTransformerHandler(template);
        /*
         * We want to raise transformer exceptions on <xml:message terminate="true">, so
         * we add a custom listener. Also, various XSLT processors react in different ways
         * to transformation errors -- some of them report error as recoverable, some of
         * them report error as unrecoverable.
         */
        handler.getTransformer().setErrorListener(new TransformerErrorListener());
        return handler;
    }
}
