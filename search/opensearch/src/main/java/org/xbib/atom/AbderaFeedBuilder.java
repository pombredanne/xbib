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
package org.xbib.atom;

import java.io.StringReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.abdera.model.Feed;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

/**
 * The Abdera feed builder for atom feeds is an XML event consumer
 *
 */
public class AbderaFeedBuilder implements XMLEventConsumer {

    private final static Logger logger = LoggerFactory.getLogger(AbderaFeedBuilder.class.getName());

    private final static RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();

    private final UUIDGenerator uuidgenerator = UUIDGenerator.getInstance();

    private final XMLInputFactory inputFactory = XMLInputFactory.newInstance();

    private final MessageFormat formatter = new MessageFormat("");

    private final Map<String, String> services = new LinkedHashMap<String, String>() {
        {
            put("xhtml", "application/xhtml+xml");
            put("xml", "application/xml");
            put("mods", "application/mods+xml");
            put("json", "application/json");
        }
    };
    /**
     * feed elements from JSON stream
     */
    private final Set<String> elements = new HashSet<String>() {
        {
            add("hits");
            add("took");
            add("timed_out");
            add("total");
            add("successful");
            add("failed");
            add("total");
            add("max_score");
            add("sort");
        }
    };
    /**
     * the Abdera instance (transient because it is non-serializable)
     */
    private final transient Abdera abdera;
    /**
     * the atom feed (transient because it is non-serializable)
     */
    private final transient Feed feed;
    /**
     * the current atom entry(transient because it is non-serializable)
     */
    private transient Entry entry;

    private final Stack<ExtensibleElement> stack;

    private StringBuilder path;

    private long totalHits;

    private String index;

    private String type;

    private String id;

    private final String servicePath;

    private String entryId;

    private AtomFeedProperties config;

    public AbderaFeedBuilder(AtomFeedProperties config, String query) {
        this.abdera = config.getAbdera();
        this.feed = abdera.newFeed();
        this.entry = abdera.newEntry();
        this.stack = new Stack();
        this.path = new StringBuilder();
        this.totalHits = 0L;
        this.config = config;
        this.servicePath = config.getServicePath();
        stack.push(feed);
        feed.setId("urn:uuid:" + uuidgenerator.generateNameBasedUUID(query));
        feed.setUpdated(new Date());
        // do we need self link?
        // feed.addLink(baseURI + "/atom", "self");
    }

    @Override
    public void add(XMLEvent event) throws XMLStreamException {
        if (event.isStartElement()) {
            StartElement element = (StartElement) event;
            QName qname = element.getName();
            path.append(qname.getLocalPart()).append('.');
            // handle these kind of 'hits' as Atom feed entries
            if ("result.hits.hits.".equals(path.toString())) {
                entry = abdera.newEntry();
                feed.addEntry(entry);
                // trick: replace parent on stack
                stack.pop();
                stack.push(entry);
            } else {
                ExtensibleElement parent = stack.peek();
                if (qname.getPrefix().isEmpty() && elements.contains(qname.getLocalPart())) {
                    // some elements may have no prefix, add it here
                    qname = new QName(ATOM.NS_URI, qname.getLocalPart(), ATOM.NS_PREFIX);
                }
                ExtensibleElement child = parent.addExtension(qname);
                Iterator iterator = element.getAttributes();
                while (iterator.hasNext()) {
                    Attribute attribute = (Attribute) iterator.next();
                    QName name = attribute.getName();
                    String value = attribute.getValue();
                    child.setAttributeValue(name, value);
                }
                stack.push(child);
            }
        } else if (event.isEndElement()) {
            EndElement element = (EndElement) event;
            QName qname = element.getName();
            // detect special variables
            if ("result.hits.total.".equals(path.toString())) {
                this.totalHits = Long.parseLong(stack.peek().getText());
            }
            if (!"result.hits.hits.".equals(path.toString())) {
                ExtensibleElement child = stack.pop();
                String ns = qname.getNamespaceURI();
                String name = qname.getLocalPart();
                if (ATOM.NS_URI.equals(ns)) {
                    if ("index".equals(name)) {
                        index = child.getText();
                    } else if ("type".equals(name)) {
                        type = child.getText();
                    } else if ("id".equals(name)) {
                        id = child.getText();
                        entryId = index + "/" + type + "/" + id;
                        entry.setId("urn:uuid:" + uuidgenerator.generateNameBasedUUID(entryId));
                        entry.setTitle(entryId); // default
                        entry.setUpdated(new Date()); // default
                        createServices(entry);
                    }
                } else {
                    if ("updated".equals(name)) {
                        entry.setUpdated(child.getText());
                    } else if ("title".equals(name)) {
                        String s = entry.getTitle();
                        if (entryId.equals(s)) {
                            s = null;
                        }
                        String title = s != null ? s + " / " + child.getText() : child.getText();
                        entry.setTitle(title);
                    }
                }
            }
            int pos = path.lastIndexOf(qname.getLocalPart());
            if (pos > 0) {
                path = path.delete(pos, path.length());
            }
        } else if (event.isCharacters()) {
            Characters characters = (Characters) event;
            stack.peek().setText(characters.getData());
        } else if (event.isNamespace()) {
            Namespace element = (Namespace) event;
            stack.peek().declareNS(element.getNamespaceURI(), element.getPrefix());
        }
    }

    public Feed getFeed(String query, long millis, long processMillis,
            long from, long size) {
        addFeedAuthor();
        addFeedTitle(query);
        // 1000.000 = convert to seconds with three precision digits
        addResultCountAndTime(totalHits, millis / 1000.000);
        addFrontendInfo();
        addProcessTime(processMillis);
        addOpenSearch(totalHits, from, size);
        String s = addStyleSheet();
        logger.info("[query={}] [hits={}] [millis={}] [searchtime={}] [stylesheet={}]",
                query, totalHits, millis, processMillis, s);
        return feed;
    }

    protected void addOpenSearch(long total, long from, long size) {
        feed.addSimpleExtension(OpenSearch.NS_URI, OpenSearch.TOTAL_RESULTS, OpenSearch.NS_PREFIX,
                Long.toString(total));
        feed.addSimpleExtension(OpenSearch.NS_URI, OpenSearch.START_INDEX, OpenSearch.NS_PREFIX,
                Long.toString(from));
        feed.addSimpleExtension(OpenSearch.NS_URI, OpenSearch.ITEMS_PER_PAGE, OpenSearch.NS_PREFIX,
                Long.toString(size));
    }

    protected void createServices(Entry entry) {
        for (String s : services.keySet()) {
            // relative URI, because baseURI is not correct in case of reverse proxies (or use ProxyPreserveHost on)
            String href =
                    (servicePath != null ? servicePath : "")
                    + "/" + s
                    + "/" + index + "/" + type + "/" + id;
            String rel = "alternate";
            entry.addLink(href, rel, services.get(s), null, null, 0);
        }
    }

    /**
     * Add feed author. The author name is usually configured in a properties
     * file.
     */
    protected void addFeedAuthor() {
        feed.addAuthor(config.getFeedAuthor());
        //properties.getProperty(FEED_AUTHOR_PROPERTY_KEY, "unknown"));
    }

    /**
     * Add feed title. Usually a formatted query.
     * @param query
     */
    protected void addFeedTitle(String query) {
        String pattern = config.getFeedTitle();
        if (pattern != null) {
            formatter.applyPattern(pattern);
            feed.setTitle(formatter.format(new Object[]{query}));
        }
    }

    /**
     * Add result count and elapsed time. Usually formatted by configuration.
     * @param count
     * @param elapsedTime
     */
    protected void addResultCountAndTime(long count, double elapsedTime) {
        String pattern = config.getSubtitlePattern();
        if (pattern != null) {
            formatter.applyPattern(pattern);
            String countAndTime = formatter.format(new Object[]{count, elapsedTime});
            feed.setSubtitle(countAndTime);
        }
    }

    /**
     * Add some info about the frontend. Might be useful for logging or for
     * tracking errors if several frontends are running.
     */
    protected void addFrontendInfo() {
        feed.addSimpleExtension(ATOM.NS_URI, "frontend", ATOM.NS_PREFIX,
                runtime.getName());
    }

    /**
     * Add some info about the result processing time.
     *
     * @param millis
     */
    protected void addProcessTime(double millis) {
        String pattern = config.getTimePattern();
        if (pattern != null) {
            formatter.applyPattern(pattern);
            feed.addSimpleExtension(ATOM.NS_URI, "feedconstructiontime", ATOM.NS_PREFIX,
                    formatter.format(new Object[]{millis}));
        }
    }

    /**
     * Add a style sheet processing instruction for XSLT. Usually the style
     * sheet location is given by configuration.
     */
    protected String addStyleSheet() {
        // add stylesheet processing instruction if required, e.g. by a servlet
        String stylesheet = config.getStyleSheet();
        if (stylesheet != null) {
            feed.getDocument().addProcessingInstruction("xml-stylesheet",
                    "href=\"" + stylesheet + "\" type=\"text/xsl\"");
            feed.getDocument().setContentType("text/xml");
        }
        return stylesheet;
    }

    protected void injectXML(XMLEventConsumer consumer, String xml, QName root) throws XMLStreamException {
        XMLEventReader xmlReader = inputFactory.createXMLEventReader(new StringReader(xml));
        while (xmlReader.hasNext()) {
            consumer.add(xmlReader.peek());
            xmlReader.nextEvent();
        }
    }
}
