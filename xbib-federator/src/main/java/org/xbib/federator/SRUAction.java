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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.XMLEvent;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.sru.SRU;
import org.xbib.sru.SRUAdapter;
import org.xbib.sru.SRUResponseAdapter;
import org.xbib.sru.SearchRetrieve;
import org.xbib.sru.adapter.SRUAdapterFactory;

public class SRUAction extends AbstractAction {

    private final static Logger logger = LoggerFactory.getLogger(SRUAction.class.getName());
    private final XMLEventFactory eventFactory = XMLEventFactory.newInstance();

    @Override
    public Action call() {
        String query = get(params, "query", null);
        if (query == null) {
            logger.warn("query not set, not executing: {}", params);
            return null;
        }
        if (response == null) {
            logger.warn("response not set, not executing: {}", params);
            return null;
        }
        String name = get(params, "name", "default");
        final int from = get(params, "from", 1);
        final int size = get(params, "size", 10);
        final SRUAdapter adapter = SRUAdapterFactory.getAdapter(name);
        SearchRetrieve request = new SearchRetrieve();
        try {
            adapter.connect();
            adapter.setStylesheetTransformer(transformer);
            request.setURI(adapter.getURI())
                    .setQuery(query).setStartRecord(from).setMaximumRecords(size)
                    .setVersion(adapter.getVersion())
                    .setRecordPacking(adapter.getRecordPacking())
                    .setRecordSchema(adapter.getRecordSchema());
            response.setOrigin(adapter.getURI()).setListener(new SRUResponseAdapter() {
                int position = from;
                
                @Override
                public void numberOfRecords(long numberOfRecords) {
                    count = numberOfRecords;
                }

                @Override
                public void beginRecord() {
                    Collection<XMLEvent> events = getResponse().getEvents();
                    events.add(eventFactory.createStartDocument());
                    events.add(eventFactory.createNamespace("id", position++ + "_" + adapter.getURI().getHost()));
                }

                @Override
                public void recordData(Collection<XMLEvent> record) {
                    Collection<XMLEvent> events = getResponse().getEvents();
                    Iterator<XMLEvent> it = record.iterator();
                    while (it.hasNext()) {
                        XMLEvent e = it.next();
                        if (!e.isStartDocument() && !e.isEndDocument()) {
                            events.add(e);
                        }
                    }
                }

                @Override
                public void extraRecordData(Collection<XMLEvent> record) {
                    Collection<XMLEvent> events = getResponse().getEvents();
                    Iterator<XMLEvent> it = record.iterator();
                    while (it.hasNext()) {
                        XMLEvent e = it.next();
                        if (!e.isStartDocument() && !e.isEndDocument()) {
                            events.add(e);
                        }
                    }
                }

                @Override
                public void recordMetadata(String recordSchema, String recordPacking,
                        String recordIdentifier, int recordPosition) {
                    Collection<XMLEvent> events = getResponse().getEvents();
                    if (events instanceof List) {
                        List<XMLEvent> list = (List<XMLEvent>) events;
                        ListIterator<XMLEvent> it = list.listIterator(events.size());
                        while (it.hasPrevious()) {
                            XMLEvent e = it.previous();
                            if (e.isStartDocument()) {
                                it.next(); // step to element
                                it.next();
                                // disguised namespaces for SRU.
                                it.add(eventFactory.createNamespace("recordSchema", recordSchema));
                                it.add(eventFactory.createNamespace("recordPacking", recordPacking));
                                it.add(eventFactory.createNamespace("recordIdentifier", recordIdentifier));
                                it.add(eventFactory.createNamespace("recordPosition", Integer.toString(recordPosition)));
                                break;
                            }
                        }
                    }
                }

                @Override
                public void endRecord() {
                    Collection<XMLEvent> events = getResponse().getEvents();
                    events.add(eventFactory.createEndDocument());
                }
            });
            adapter.searchRetrieve(request, response);
        } catch (Exception e) {
            logger.warn(adapter.getURI().getHost() + " failure: " + e.getMessage(), e);
        } finally {
            adapter.disconnect();
        }
        return this;
    }
}
