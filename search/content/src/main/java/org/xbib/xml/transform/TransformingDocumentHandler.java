/*
 * Carrot2 project.
 *
 * Copyright (C) 2002-2010, Dawid Weiss, Stanisław Osiński.
 * All rights reserved.
 *
 * Refer to the full license file "carrot2.LICENSE"
 * in the root folder of the repository checkout or at:
 * http://www.carrot2.org/carrot2.LICENSE
 */
package org.xbib.xml.transform;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * A SAX handler that detects <code>xml-stylesheet</code> directive and delegates SAX
 * events to a declared transformer.
 *
 * URI resolving replaced by servlet real path method.
 *
 */
public final class TransformingDocumentHandler implements ContentHandler {

    private final static Logger logger = LoggerFactory.getLogger(TransformingDocumentHandler.class.getName());
    /**
     * A map of XSLT output methods and their corresponding MIME content types.
     */
    private final static HashMap<String, String> METHOD_MAPPING;    

    static {
        METHOD_MAPPING = new HashMap();
        METHOD_MAPPING.put("text", "text/plain");
        METHOD_MAPPING.put("xml", "application/xml");
        METHOD_MAPPING.put("html", "text/html");
        METHOD_MAPPING.put("mods", "application/x-mods");
    }
    /**
     * A regular expression for extracting <code>xml-stylesheet</code>'s
     * <code>type</code> pseudo-attribute.
     */
    private final Pattern typePattern = Pattern.compile(
            "(type[ \t]*=[ \\t]*\")([^\"]*)(\")", Pattern.CASE_INSENSITIVE);
    /**
     * A regular expression for extracting <code>xml-stylesheet</code>'s
     * <code>href</code> pseudo-attribute.
     */
    private final Pattern hrefPattern = Pattern.compile(
            "(href[ \\t]*=[ \\t]*\")([^\"]*)(\")", Pattern.CASE_INSENSITIVE);
    /**
     * A regular expression for extracting <code>ext-stylesheet</code>'s
     * <code>resource</code> pseudo-attribute.
     */
    private final Pattern resourcePattern = Pattern.compile(
            "(resource[ \\t]*=[ \\t]*\")([^\"]*)(\")", Pattern.CASE_INSENSITIVE);
    
    private final  SAXTransformerFactory transformerFactory = 
            (SAXTransformerFactory) TransformerFactory.newInstance("org.apache.xalan.processor.TransformerFactoryImpl", null);
    
    /**
     * The default handler used when no <code>xml-stylesheet</code> directive is
     * specified in the XML stream.
     */
    private TransformerHandler defaultHandler;
    /**
     * The actual content handler (transformer) used for processing the input.
     */
    private TransformerHandler contentHandler;
    /**
     * A result sink where the transformation output should be redirected.
     */
    private Result result;
    /**
     * Transformer error listener.
     */
    private TransformerErrorListener transformerErrorListener = new TransformerErrorListener();
    /**
     * Locator instance used by this handler is also shared with the transformation
     * handler.
     */
    private Locator locator;
    /**
     * A pool of precompiled stylesheets.
     */
    private final StylesheetPool pool;
    /**
     *
     */
    private ContentTypeListener contentTypeListener;
    /**
     * A set of stylesheet parameters, copied from the request context when the
     * transformation begins.
     */
    private final Map<String, Object> stylesheetParams;

    /**
     * Creates a SAX handler with the given base application URL and context path. The
     * base URL is needed to resolve host-relative stylesheet URIs. Application context
     * path is used to initialize local streams instead of requesting the stylesheet via
     * HTTP.
     */
    public TransformingDocumentHandler(Map<String, Object> stylesheetParams, StylesheetPool pool) {
        this.pool = pool;
        this.stylesheetParams = stylesheetParams;
    }

    /**
     * {@link ContentHandler} implementation.
     */
    @Override
    public void startDocument() throws SAXException {
        // Empty. We don't know the actual content handler yet.
    }

    /**
     * {@link ContentHandler} implementation.
     */
    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        initContentHandler();
        contentHandler.characters(ch, start, length);
    }

    /**
     * {@link ContentHandler} implementation.
     */
    @Override
    public void endDocument() throws SAXException {
        initContentHandler();
        try {
            contentHandler.endDocument();
        } catch (Exception t) {
            final Exception e = transformerErrorListener.getException();
            if (e != null) {
                final Throwable cause = e.getCause();
                if (cause instanceof Exception) {
                    throw new SAXException("XSLT transformation error",
                            (Exception) cause);
                } else {
                    throw new SAXException("XSLT transformation error",
                            e);
                }
            }
        }
    }

    /**
     * {@link ContentHandler} implementation.
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        initContentHandler();
        contentHandler.endElement(namespaceURI, localName, qName);
    }

    /**
     * {@link ContentHandler} implementation.
     */
    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        initContentHandler();
        contentHandler.endPrefixMapping(prefix);
    }

    /**
     * {@link ContentHandler} implementation.
     */
    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        /*
         * Pass ignorable whitespace if we have a content handler. Before content handler
         * initialization simply ignore these calls. We could queue SAX events until
         * content handler is available, but would it make any sense?
         */
        if (contentHandler != null) {
            contentHandler.ignorableWhitespace(ch, start, length);
        }
    }

    /**
     * {@link ContentHandler} implementation.
     */
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        initContentHandler();
        contentHandler.startPrefixMapping(prefix, uri);
    }

    /**
     * {@link ContentHandler} implementation. Detect processing instructions and see if we
     * have <code>xml-stylesheet</code> anywhere.
     * Remove processing instructions from output.
     */
    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        if (contentHandler == null) {
            inspectProcessingInstruction(this, target, data);
        }
    }

    /**
     * {@link ContentHandler} implementation.
     */
    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    /**
     * {@link ContentHandler} implementation.
     */
    @Override
    public void skippedEntity(String name) throws SAXException {
        this.initContentHandler();
        contentHandler.skippedEntity(name);
    }

    /**
     * {@link ContentHandler} implementation.
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName,
            Attributes atts) throws SAXException {
        this.initContentHandler();
        contentHandler.startElement(namespaceURI, localName, qName, atts);
    }

    /**
     * Replaces the default transformer handler with the given one.
     */
    private void setTransformerHandler(TransformerHandler handler)
            throws SAXException {
        if (contentHandler != null) {
            throw new SAXException(
                    "Some input has been already processed. Cannot change the handler anymore. "
                    + "Place xml-stylesheet "
                    + "directive immediately at the top of the XML file.");
        }

        final Transformer transformer = handler.getTransformer();
        /*
         * Pass any stylesheet parameters to the transformer.
         */
        if (stylesheetParams != null) {
            for (Iterator<Map.Entry<String, Object>> i = stylesheetParams.entrySet().iterator(); i.hasNext();) {
                final Map.Entry<String, Object> entry = i.next();
                transformer.setParameter(entry.getKey(), entry.getValue());
                logger.info("setting transformer parameter {}", entry.getKey());
            }
        }
        this.defaultHandler = handler;
    }

    /**
     * Sets a {@link ContentTypeListener} for this transformation.
     */
    public void setContentTypeListener(ContentTypeListener l) {
        this.contentTypeListener = l;
    }

    /**
     * This method should be invoked to cleanup after processing is done.
     */
    public void cleanup() {
        if (this.defaultHandler != null) {
            /*
             * Reset the default handler's transformer.
             */
            this.defaultHandler.getTransformer().reset();
        }
    }

    /**
     * Sets the result sink for the xslt transformation.
     */
    public void setTransformationResult(Result result) {
        this.result = result;
    }

    /**
     * Process <code>xml-stylesheet</code>.
     */
    private URI processXmlStylesheet(String target, String data) {
        if (!target.equals("xml-stylesheet")) {
            return null;
        }
        /*
         * Break up pseudo-attributes and look for content-type
         */
        final Matcher typeMatcher = typePattern.matcher(data);
        if (!typeMatcher.find()) {
            logger.warn("xml-stylesheet directive with no type attribute (should be text/xsl).");
            return null;
        }
        final String type = typeMatcher.group(2);
        if (!"text/xsl".equals(type)) {
            logger.warn("xml-stylesheet directive with incorrect type (should be text/xsl): {0}", type);
            return null;
        }
        final Matcher hrefMatcher = hrefPattern.matcher(data);
        if (!hrefMatcher.find()) {
            logger.warn("xml-stylesheet directive with no 'href' pseudo-attribute.");
            return null;
        }
        return URI.create(hrefMatcher.group(2));
    }

    /**
     * Inspect a processing instruction looking for <code>xml-stylesheet</code>
     * or <code>ext-stylesheet</code> directives. If found, update the
     * {@link TransformingDocumentHandler#setTransformerHandler(TransformerHandler)}
     * appropriately.
     */
    private void inspectProcessingInstruction(TransformingDocumentHandler handler,
            String target, String data) throws SAXException {
        URI uri = processXmlStylesheet(target, data);
        if (uri == null) {
            return;
        }
        /*
         * Check the pool for precompiled cached Templates
         */
        try {
            final String systemId = uri.toString();
            Templates template = pool.newTemplates(transformerFactory, new StreamSource(systemId));
            // Find out about the content type and encoding.
            if (contentTypeListener != null) {
                final Properties outputProps = template.getOutputProperties();
                String encoding;
                /*
                 * If you're tempted to use Properties@containsKey, see
                 * http://issues.carrot2.org/browse/CARROT-507
                 */
                String contentType = null;
                if (hasKey(outputProps, OutputKeys.MEDIA_TYPE)) {
                    contentType = outputProps.getProperty(OutputKeys.MEDIA_TYPE);
                } else if (hasKey(outputProps, OutputKeys.METHOD)) {
                    final String method = outputProps.getProperty(OutputKeys.METHOD);
                    contentType = METHOD_MAPPING.get(method);
                }
                if (contentType == null) {
                    // Default content type.
                    contentType = METHOD_MAPPING.get("xml");
                }
                if (hasKey(outputProps, OutputKeys.ENCODING)) {
                    encoding = outputProps.getProperty(OutputKeys.ENCODING);
                } else {
                    encoding = "UTF-8";
                }
                if ("UTF8".equals(encoding)) {
                    encoding = "UTF-8";
                }
                try {
                    contentTypeListener.setContentType(contentType, encoding);
                } catch (IOException ex) {
                    throw new SAXException(ex);
                }
            }
            final TransformerHandler tHandler = pool.newTransformerHandler(transformerFactory, template);
            tHandler.getTransformer().setErrorListener(transformerErrorListener);
            handler.setTransformerHandler(tHandler);
        } catch (TransformerConfigurationException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Properties by default extend from HashMap, but can contain a backup set
     * of keys as set in {@link Properties#Properties(Properties)}. Unfortunately,
     * while {@link Properties#getProperty(String)} works with these default
     * values, {@link Properties#containsKey(Object)} does not. In this method
     * we check for the existence of a key by trying to load it.
     */
    private static boolean hasKey(Properties props, String key) {
        return props.getProperty(key) != null;
    }

    /**
     * Initializes the content handler because content is about to be sent to the result.
     * If no content handler is available, throws an exception.
     */
    private void initContentHandler() throws SAXException {
        if (contentHandler == null) {
            if (defaultHandler == null) {
                logger.info("Stylesheet not specified, using identity handler.");
                try {
                    this.defaultHandler = pool.getIdentityTransformerHandler(transformerFactory);                    
                } catch (TransformerConfigurationException e) {
                    throw new SAXException(e);
                }
                if (contentTypeListener != null) {
                    try {
                        contentTypeListener.setContentType(METHOD_MAPPING.get("xml"), "UTF-8");
                    } catch (IOException e) {
                        throw new SAXException(e);
                    }
                }
            }
            this.contentHandler = defaultHandler;
            this.contentHandler.setResult(result);
            this.contentHandler.startDocument();
            if (locator != null) {
                this.contentHandler.setDocumentLocator(locator);
            }
        }
        if (transformerErrorListener.getException() != null) {
            throw new SAXException("XSLT transformation error.",
                    transformerErrorListener.getException());
        }
    }
}
