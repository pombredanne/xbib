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
package org.xbib.federator;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.SRU;
import org.xbib.sru.SearchRetrieveResponse;
import org.xbib.xml.transform.StylesheetTransformer;

public class FederatorService {

    private final static Logger logger = LoggerFactory.getLogger(FederatorAction.class.getName());

    interface MODS {

        String NS_PREFIX = "mods";
        String NS_URI = "http://www.loc.gov/mods/v3";
    }
    private final static FederatorService instance = new FederatorService();
    private final Map<String, List<Action>> requests = new HashMap();
    private final Map<String, List<ResponseListener<Action>>> listeners = new HashMap();
    private String stylesheetPath;
    private ExecutorService executorService;
    private int threadnum = 1;

    private FederatorService() {
    }

    public static FederatorService getInstance() {
        return instance;
    }

    public Map<String, List<Action>> getRequests() {
        return requests;
    }

    public FederatorService setThreads(int threadnum) {
        this.threadnum = threadnum;
        return this;
    }

    public void shutdown(long millisToWait) throws InterruptedException {
        if (executorService != null) {
            executorService.awaitTermination(millisToWait, TimeUnit.MILLISECONDS);
            executorService.shutdown();
        }
    }

    /**
     * Add listener
     *
     * @param groupId
     * @param listener
     */
    public FederatorService addListener(String groupId, ResponseListener<Action> listener) {
        if (!listeners.containsKey(groupId)) {
            listeners.put(groupId, new ArrayList());
        }
        listeners.get(groupId).add(listener);
        return this;
    }

    /**
     * Remove listener
     *
     * @param groupId
     * @param listener
     */
    public FederatorService removeListener(String groupId, ResponseListener<Action> listener) {
        if (!listeners.containsKey(groupId)) {
            listeners.put(groupId, new ArrayList());
        }
        listeners.get(groupId).remove(listener);
        return this;
    }

    public FederatorService setStylesheetPath(String path) {
        this.stylesheetPath = path;
        return this;
    }

    /**
     * Submit actions for federation. Create federatable SRU actions from a JSON
     * specification, submit theactions under a group ID.
     *
     * @param groupId
     * @param json
     * @return
     * @throws IOException
     */
    public FederatorService submit(String groupId, String json)
            throws IOException, InterruptedException, ExecutionException {
        ArrayList<HashMap<String, Object>> specs = null;
        try {
            specs = new ObjectMapper().readValue(json, ArrayList.class);
        } catch (JsonMappingException e) {
            throw new IOException(e);
        }
        List<Action> actions = new ArrayList();
        for (Map<String, Object> params : specs) {
            actions.add(new PQFZAction().setParams(params));
        }
        submit(groupId, actions);
        return this;
    }

    /**
     * Submit requests
     *
     * @param groupId
     * @param request
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public FederatorService submit(String groupId, List<Action> request)
            throws InterruptedException, ExecutionException {
        for (Action action : request) {
            LinkedList<XMLEvent> events = new LinkedList();
            SearchRetrieveResponse response = new SearchRetrieveResponse(new StringWriter());
            response.setEvents(events);
            action.setGroup(groupId);
            action.setResponse(response);
            action.setTransformer(new StylesheetTransformer(stylesheetPath)); // stylesheet transformer is not shareable
            if (!requests.containsKey(groupId)) {
                requests.put(groupId, new LinkedList());
            }
            requests.get(groupId).add(action);
        }
        return this;
    }

    /**
     * Invoke requests and wait for responses. Call the listeners for this group
     * if response arrive.
     *
     * @return this FederatorService
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public synchronized FederatorService waitFor(String groupId, Writer writer)
            throws InterruptedException, ExecutionException,
            TransformerException, XMLStreamException {
        if (executorService == null && threadnum > 0) {
            this.executorService = Executors.newFixedThreadPool(threadnum);
        }
        // execute all group actions
        long count = 0L;
        if (executorService != null && requests.containsKey(groupId)) {
            LinkedList<XMLEvent> events = new LinkedList();
            for (Future<Action> f : executorService.invokeAll(requests.get(groupId))) {
                Action action = f.get();
                count += action.getCount();
                events.addAll(action.getResponse().getEvents());
                if (listeners.get(groupId) != null) {
                    for (ResponseListener<Action> listener : listeners.get(groupId)) {
                        listener.onResponse(action);
                    }
                }
            }
            wrapIntoSRUResponse(events, "1.2", Long.toString(count), writer);
        }
        requests.remove(groupId);
        return this;
    }

    private final static XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private final static XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();    
    
    private void wrapIntoSRUResponse(Collection<XMLEvent> list, String version, String numberOfRecords, Writer writer) throws XMLStreamException {
        XMLEventWriter ew = outputFactory.createXMLEventWriter(writer);
        ew.add(eventFactory.createStartDocument());
        ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "searchRetrieveResponse"));
        ew.add(eventFactory.createNamespace(SRU.NS_PREFIX, SRU.NS_URI));
        ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "version"));
        ew.add(eventFactory.createCharacters(version));
        ew.add(eventFactory.createEndElement(SRU.NS_PREFIX, SRU.NS_URI, "version"));
        ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "numberOfRecords"));
        ew.add(eventFactory.createCharacters(numberOfRecords));
        ew.add(eventFactory.createEndElement(SRU.NS_PREFIX, SRU.NS_URI, "numberOfRecords"));
        ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "records"));
        int pos = 1;
        Iterator<XMLEvent> it = list.iterator();
        while (it.hasNext()) {
            XMLEvent e = it.next();
            if (e.isProcessingInstruction()) {
            } else if (e.isStartDocument()) {
                ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "record"));
            } else if (e.isEndDocument()) {
                ew.add(eventFactory.createEndElement(SRU.NS_PREFIX, SRU.NS_URI, "recordData"));
                ew.add(eventFactory.createEndElement(SRU.NS_PREFIX, SRU.NS_URI, "record"));
            } else if (e.isNamespace()) {
                // disguised namespace  other nasty things
                String prefix = ((Namespace) e).getPrefix();
                String nsURI = ((Namespace) e).getNamespaceURI();
                switch (prefix) {
                    case "recordSchema":
                        // declare SRU record schema
                        if (SRU.RECORD_SCHEMAS.containsKey(nsURI)) {
                            // add XML namespace
                            if (SRU.RECORD_SCHEMA_NAMESPACES.containsKey(nsURI)) {
                                ew.add(eventFactory.createNamespace(nsURI, SRU.RECORD_SCHEMA_NAMESPACES.get(nsURI).toASCIIString()));
                            }
                            ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "recordSchema"));
                            ew.add(eventFactory.createCharacters(SRU.RECORD_SCHEMAS.get(nsURI).toASCIIString()));
                            ew.add(eventFactory.createEndElement(SRU.NS_PREFIX, SRU.NS_URI, "recordSchema"));
                        }
                        break;
                    case "recordPacking":
                        // SRU record packing (always "xml")
                        ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "recordPacking"));
                        ew.add(eventFactory.createCharacters(nsURI));
                        ew.add(eventFactory.createEndElement(SRU.NS_PREFIX, SRU.NS_URI, "recordPacking"));
                        break;
                    case "recordIdentifier":
                        // SRU record identifier
                        ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "recordIdentifier"));
                        ew.add(eventFactory.createCharacters(nsURI));
                        ew.add(eventFactory.createEndElement(SRU.NS_PREFIX, SRU.NS_URI, "recordIdentifier"));
                        break;
                    case "recordPosition":
                        // SRU record position is the global position (NOT the local record position)
                        ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "recordPosition"));
                        ew.add(eventFactory.createCharacters(Integer.toString(pos++)));
                        ew.add(eventFactory.createEndElement(SRU.NS_PREFIX, SRU.NS_URI, "recordPosition"));
                        // now, after recordPosition, start with recordData
                        ew.add(eventFactory.createStartElement(SRU.NS_PREFIX, SRU.NS_URI, "recordData"));
                        break;
                    case "id":
                        // non-SRU: let us identify the origin of the record by XML ID
                        ew.add(eventFactory.createAttribute(prefix, nsURI));
                        break;
                    case "format":
                    case "type":
                        // skip format, type
                        break;
                    default:
                        ew.add(e);
                        break;
                }
            } else {
                ew.add(e);
            }
        }
        ew.add(eventFactory.createEndElement(SRU.NS_PREFIX, SRU.NS_URI, "records"));
        ew.add(eventFactory.createEndDocument());
    }
}
