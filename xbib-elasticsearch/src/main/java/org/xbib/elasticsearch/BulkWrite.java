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
package org.xbib.elasticsearch;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.xbib.io.Identifiable;
import org.xbib.io.Session;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;

/**
 * Write bulk data to Elasticsearch
 * 
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class BulkWrite extends AbstractWrite {

    /** the logger */
    private static final Logger logger = LoggerFactory.getLogger(BulkWrite.class.getName());
    private int bulkSize = 100;
    private int maxActiveRequests = 30;
    private long millisBeforeContinue = 60000L;
    private int totalTimeouts;
    private static final int MAX_TOTAL_TIMEOUTS = 10;
    private static final AtomicInteger onGoingBulks = new AtomicInteger(0);
    private static final AtomicInteger counter = new AtomicInteger(0);
    private ThreadLocal<BulkRequestBuilder> currentBulk = new ThreadLocal();

    public BulkWrite(String index, String type) {
        super(index, type, ':');
        this.totalTimeouts = 0;
    }
    
    public BulkWrite setBulkSize(int bulkSize) {
        this.bulkSize = bulkSize;
        return this; 
    }
    
    public BulkWrite setMaxActiveRequests(int maxActiveRequests) {
        this.maxActiveRequests = maxActiveRequests;
        return this;
    }
    
    public BulkWrite setMillisBeforeContinue(long millis) {
        this.millisBeforeContinue = millis;
        return this;        
    }

    /**
     * Write resource to Elasticsearch in bulk mode
     * 
     * @param session the session
     * @param resource the resource
     * @throws IOException
     */
    @Override
    public void write(ElasticsearchSession session, Resource resource) throws IOException {
        if (!session.isOpen()) {
            throw new IOException("session not open");
        }
        write(session.getClient(), resource);
    }
    
    public void write(Client client, Resource resource) throws IOException {       
        if (currentBulk.get() == null) {
            currentBulk.set(client.prepareBulk());
        }
        XContentBuilder builder = build(resource);
        if (resource.isDeleted()) {
            currentBulk.get().add(Requests.deleteRequest(index).type(type).id(createId(resource)));
        } else {
            currentBulk.get().add(Requests.indexRequest(index).type(type).id(createId(resource)).create(false).source(builder));
        }
        if (currentBulk.get().numberOfActions() >= bulkSize) {
            processBulk(client);
        }
    }

    @Override
    public void flush(ElasticsearchSession session) throws IOException {
        flush(session.getClient());
    }
    
    public void flush(Client client)  throws IOException {
        if (totalTimeouts > MAX_TOTAL_TIMEOUTS) {
            // waiting some minutes is much too long, do not wait any longer            
            throw new IOException("total flush() timeouts exceeded limit of + " + MAX_TOTAL_TIMEOUTS + ", aborting");
        }
        // submit the rest of the docs for this thread
        if (currentBulk.get() != null && currentBulk.get().numberOfActions() > 0) {
            processBulk(client);
        }
        // wait for outstanding bulk requests of all threads
        while (onGoingBulks.intValue() > 0) {
            logger.info("waiting for {} active bulk requests", onGoingBulks);
            synchronized (onGoingBulks) {
                try {
                    onGoingBulks.wait(millisBeforeContinue);
                } catch (InterruptedException e) {
                    logger.warn("timeout while waiting, continuing after {} ms", millisBeforeContinue);
                    totalTimeouts++;
                }
            }
        }
    }

    private void processBulk(Client client) {
        while (onGoingBulks.intValue() >= maxActiveRequests) {
            logger.info("waiting for {} active bulk requests", onGoingBulks);
            synchronized (onGoingBulks) {
                try {
                    onGoingBulks.wait(millisBeforeContinue);
                } catch (InterruptedException e) {
                    logger.warn("timeout while waiting, continuing after {} ms", millisBeforeContinue);
                    totalTimeouts++;
                }
            }
        }
        int currentOnGoingBulks = onGoingBulks.incrementAndGet();
        final int numberOfActions = currentBulk.get().numberOfActions();
        logger.info("submitting new bulk index request ({} docs, {} requests currently active)", 
                numberOfActions, currentOnGoingBulks);
        try {
            currentBulk.get().execute(new ActionListener<BulkResponse>() {

                @Override
                public void onResponse(BulkResponse bulkResponse) {
                    if (bulkResponse.hasFailures()) {
                        logger.error("bulk index has failures: {}", bulkResponse.buildFailureMessage());
                    } else {
                        final int totalActions = counter.addAndGet(numberOfActions);
                        logger.info("bulk index success ({} millis, {} docs, total of {} docs)", 
                                bulkResponse.tookInMillis(), numberOfActions, totalActions);
                    }
                    onGoingBulks.decrementAndGet();
                    synchronized (onGoingBulks) {
                        onGoingBulks.notifyAll();
                    }
                }

                @Override
                public void onFailure(Throwable e) {
                    logger.error("bulk request failed", e);
                }
            });
        } catch (Exception e) {
            logger.error("unhandled exception, failed to execute bulk request", e);
        } finally {
            currentBulk.set(client.prepareBulk());
        }
    }

    @Override
    public void execute(Session session) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void create(ElasticsearchSession session, Identifiable identifiable, Resource resource) throws IOException {
        write(session, resource);
    }
    
}
