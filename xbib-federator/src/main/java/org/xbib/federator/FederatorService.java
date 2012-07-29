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
import java.util.HashMap;
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
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.MarcXchange;
import org.xbib.sru.SearchRetrieveResponse;
import org.xbib.xml.XMLEventIterator;
import org.xbib.xml.transform.StylesheetTransformer;

public class FederatorService {

    private final static Logger logger = LoggerFactory.getLogger(FederatorAction.class.getName());
    private final static FederatorService instance = new FederatorService();
    private final Map<String, List<XMLEvent>> eventlists = new HashMap();
    private final Map<String, List<ZAction>> requests = new HashMap();
    private final Map<String, List<ResponseListener<ZAction>>> listeners = new HashMap();
    private ExecutorService executorService;
    private int threadnum = 1;

    private FederatorService() {
    }

    public static FederatorService getInstance() {
        return instance;
    }

    public Map<String, List<ZAction>> getRequests() {
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
    public FederatorService addListener(String groupId, ResponseListener<ZAction> listener) {
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
    public FederatorService removeListener(String groupId, ResponseListener<ZAction> listener) {
        if (!listeners.containsKey(groupId)) {
            listeners.put(groupId, new ArrayList());
        }
        listeners.get(groupId).remove(listener);
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
    public FederatorService submit(String groupId,
            StylesheetTransformer transformer,
            String json)
            throws IOException, InterruptedException, ExecutionException {
        ArrayList<HashMap<String, Object>> specs = null;
        try {
            specs = new ObjectMapper().readValue(json, ArrayList.class);
        } catch (JsonMappingException e) {
            throw new IOException(e);
        }
        List<ZAction> actions = new ArrayList();
        for (Map<String, Object> params : specs) {
            actions.add(new ZAction().setParams(params));
        }
        submit(groupId, transformer, actions);
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
    public FederatorService submit(String groupId,
            StylesheetTransformer transformer,
            List<ZAction> request)
            throws InterruptedException, ExecutionException {
        LinkedList<XMLEvent> eventlist = new LinkedList();
        eventlists.put(groupId, eventlist);
        for (ZAction action : request) {
            action.setGroup(groupId);
            action.setResponse(new SearchRetrieveResponse(new StringWriter())); // not needed
            action.setTransformer(transformer);
            action.setList(eventlist);
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
    public synchronized FederatorService waitFor(String groupId,
            StylesheetTransformer transformer, Writer writer)
            throws InterruptedException, ExecutionException,
            TransformerException, XMLStreamException {
        if (executorService == null && threadnum > 0) {
            this.executorService = Executors.newFixedThreadPool(threadnum);
        }
        // execute all group actions
        if (executorService != null && requests.containsKey(groupId)) {
            for (Future<ZAction> f : executorService.invokeAll(requests.get(groupId))) {
                ZAction action = f.get();
                if (listeners.get(groupId) != null) {
                    for (ResponseListener<ZAction> listener : listeners.get(groupId)) {
                        listener.onResponse(action);
                    }
                }
            }
            // get collected MarcXchange XML events, clean all unwanted events, and pass them to the writer
            XMLEventFactory eventFactory = XMLEventFactory.newInstance();
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLEventWriter ew = outputFactory.createXMLEventWriter(writer);
            ew.add(eventFactory.createStartDocument());
            ew.add(eventFactory.createStartElement(MarcXchange.NS_PREFIX, MarcXchange.NS_URI, "collection"));
            ew.add(eventFactory.createNamespace(MarcXchange.NS_PREFIX, MarcXchange.NS_URI));
            List<XMLEvent> list = eventlists.get(groupId);
            for (XMLEvent e : list) {
                if (e.isProcessingInstruction()) {
                }
                else if (e.isStartDocument()) {
                    ew.add(eventFactory.createStartElement(MarcXchange.NS_PREFIX, MarcXchange.NS_URI, "record"));
                }
                else if (e.isEndDocument()) {
                    ew.add(eventFactory.createEndElement(MarcXchange.NS_PREFIX, MarcXchange.NS_URI, "record"));
                }
                else if (e.isNamespace()) {
                    ew.add(eventFactory.createAttribute( ((Namespace)e).getPrefix(), ((Namespace)e).getNamespaceURI()));
                }
                else {
                    ew.add(e);
                }
            }
            ew.add(eventFactory.createEndElement(MarcXchange.NS_PREFIX, MarcXchange.NS_URI, "collection"));
            ew.add(eventFactory.createEndDocument());
        }
        requests.remove(groupId);
        return this;
    }
}
