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

import com.google.common.collect.Maps;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.xbib.elasticsearch.action.bulk.concurrent.ConcurrentBulkProcessor;
import org.xbib.elasticsearch.action.bulk.concurrent.ConcurrentBulkRequest;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

/**
 * An Elasticsearch indexer
 */
public class ElasticsearchIndexer
        extends Elasticsearch
        implements ElasticsearchIndexerInterface {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchIndexer.class.getName());

    /**
     * The default  size of a bulk request
     */
    private int maxBulkActions = 100;
    /**
     * The default number of maximum concurrent bulk requests
     */
    private int maxConcurrentBulkRequests = 30;
    private final AtomicLong outstandingBulkRequests = new AtomicLong();
    private boolean enabled;    
    private ConcurrentBulkProcessor bulk;
    private String index;
    private String type;
    private boolean dateDetection;
    private String mapping;

    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public ElasticsearchIndexer settings(Settings settings) {
        super.settings(settings);
        return this;
    }

    @Override
    public ElasticsearchIndexer newClient(boolean forceNew) {
        return newClient(findURI(), forceNew);
    }

    /**
     * Create new client with concurrent bulk processor
     * @param uri the cluster name URI
     * @param forceNew if a new client should be created
     * @return this indexer 
     */
    @Override
    public ElasticsearchIndexer newClient(URI uri, boolean forceNew) {
        super.newClient(uri, forceNew);
        ConcurrentBulkProcessor.Listener listener = new ConcurrentBulkProcessor.Listener() {

            @Override
            public void beforeBulk(long executionId, ConcurrentBulkRequest request) {
                long l = outstandingBulkRequests.incrementAndGet();
                logger.info("new bulk [{}] of [{} items], {} outstanding bulk requests", 
                        executionId, request.numberOfActions(), l);
            }

            @Override
            public void afterBulk(long executionId, BulkResponse response) {
                long l = outstandingBulkRequests.decrementAndGet();
                logger.info("bulk [{}] success [{} items] [{}ms]", 
                        executionId , response.items().length, response.took().millis() );
            }

            @Override
            public void afterBulk(long executionId, Throwable failure) {
                long l = outstandingBulkRequests.decrementAndGet();
                logger.error("bulk [" + executionId + "] error", failure);
                enabled = false;
            }
        };
        this.bulk = ConcurrentBulkProcessor.builder(client, listener)
                .maxBulkActions(maxBulkActions)
                .maxConcurrentBulkRequests(maxConcurrentBulkRequests)
                .build();
        this.enabled = true;
        return this;
    }

    /**
     * Initial settings tailored for index/bulk client use.
     * No transport sniffing, only thread pool is for bulk/indexing,
     * other thread pools are minimal, ten Netty connections in parallel.
     * 
     * @param uri the cluster name URI
     * @return the initial settings
     */
    @Override
    protected Settings initialSettings(URI uri) {
        return ImmutableSettings.settingsBuilder().put("cluster.name", findClusterName(uri))
                .put("client.transport.sniff", false)
                .put("transport.netty.connections_per_node.low", 0)
                .put("transport.netty.connections_per_node.med", 0)
                .put("transport.netty.connections_per_node.high", 10)
                .put("threadpool.index.type", "fixed")
                .put("threadpool.index.size", "10")
                .put("threadpool.bulk.type", "fixed")
                .put("threadpool.bulk.size", "10")
                .put("threadpool.get.type", "fixed")
                .put("threadpool.get.size", "1")
                .put("threadpool.search.type", "fixed")
                .put("threadpool.search.size", "1")
                .put("threadpool.percolate.type", "fixed")
                .put("threadpool.percolate.size", "1")
                .put("threadpool.management.type", "fixed")
                .put("threadpool.management.size", "1")
                .put("threadpool.flush.type", "fixed")
                .put("threadpool.flush.size", "1")
                .put("threadpool.merge.type", "fixed")
                .put("threadpool.merge.size", "1")
                .put("threadpool.refresh.type", "fixed")
                .put("threadpool.refresh.size", "1")
                .put("threadpool.cache.type", "fixed")
                .put("threadpool.cache.size", "1")
                .put("threadpool.snapshot.type", "fixed")
                .put("threadpool.snapshot.size", "1")
                .build();
    }

    @Override
    public ElasticsearchIndexer index(String index) {
        this.index = index;
        return this;
    }

    @Override
    public String index() {
        return index;
    }

    @Override
    public ElasticsearchIndexer type(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public ElasticsearchIndexer dateDetection(boolean dateDetection) {
        this.dateDetection = dateDetection;
        return this;
    }

    @Override
    public boolean dateDetection() {
        return dateDetection;
    }
    
    @Override
    public ElasticsearchIndexer maxBulkActions(int maxBulkActions) {
        this.maxBulkActions = maxBulkActions;
        return this;
    }

    @Override
    public ElasticsearchIndexer maxConcurrentBulkRequests(int maxConcurrentBulkRequests) {
        this.maxConcurrentBulkRequests = maxConcurrentBulkRequests;
        return this;
    }

    public ElasticsearchIndexer mapping(String mapping) {
        this.mapping = mapping;
        return this;
    }
    
    public ElasticsearchIndexer newIndex() {
        return newIndex(true);
    }
    
    public ElasticsearchIndexer newIndex(boolean ignoreException) {
        if (client == null) {
            return this;
        }
        CreateIndexRequest request = new CreateIndexRequest(index);        
        if (mapping != null) {
            request.mapping(type, mapping);
        } else {
            Map m = Maps.newHashMap();
            m.put("date_detection", dateDetection);
            request.mapping(type, m);
        }
        try {
            client.admin().indices().create(request).actionGet();
        } catch (Exception e) {
            if (!ignoreException) {
                throw e;
            }
        }
        return this;
    }

    public ElasticsearchIndexer deleteIndex() {
        return deleteIndex(true, true);
    }
    
    public ElasticsearchIndexer deleteIndex(boolean enabled) {
        return deleteIndex(enabled, true);
    }

    public ElasticsearchIndexer deleteIndex(boolean enabled, boolean ignoreException) {
        if (client == null) {
            return this;
        }
        try {
            if (enabled) {
                client.admin().indices().delete(new DeleteIndexRequest(index));
            }
        } catch (Exception e) {
            if (!ignoreException) {
                throw e;
            }
        }
        return this;
    }

    public ElasticsearchIndexer newType(String mapping) {
        if (client == null) {
            return this;
        }
        client.admin().indices().putMapping(new PutMappingRequest()
                .indices(new String[]{index})
                .type(type)
                .source(mapping))
                .actionGet();
        return this;
    }
    
    public ElasticsearchIndexer deleteType() {
        return deleteType(true, true);
    }
    
    public ElasticsearchIndexer deleteType(boolean enabled) {
        return deleteType(enabled, true);
    }    

    public ElasticsearchIndexer deleteType(boolean enabled, boolean ignoreException) {
        if (client == null) {
            return this;
        }
        try {
            if (enabled) {
                client.admin().indices().deleteMapping(new DeleteMappingRequest().indices(new String[]{index}).type(type));
            }
        } catch (Exception e) {
            if (!ignoreException) {
                throw e;
            }
        }
        return this;
    }

    public ElasticsearchIndexer refresh() {
        if (client == null) {
            return this;
        }
        client.admin().indices().refresh(new RefreshRequest());
        return this;
    }

    @Override
    public ElasticsearchIndexer index(String index, String type, String id, String source) {
        if (!enabled) {
            return this;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("index: coordinate = {}/{}/{} source = {}", index, type, id, source);
        }
        IndexRequest indexRequest = Requests.indexRequest(index).type(type).id(id).create(false).source(source);
        try {
            bulk.add(indexRequest);
        } catch (Exception e) {
            logger.error("bulk index failed: "+ e.getMessage(), e);
            enabled = false;
        }
        return this;
    }

    @Override
    public ElasticsearchIndexer delete(String index, String type, String id) {
        DeleteRequest deleteRequest = Requests.deleteRequest(index).type(type).id(id);
        try {
           bulk.add(deleteRequest);
        } catch (Exception e) {
            logger.error("bulk delete failed: " + e.getMessage(), e);
            enabled = false;
        }
        return this;
    }

    @Override
    public ElasticsearchIndexer flush() {
        if (!enabled) {
            return this;
        }
        bulk.flush();
        return this;
    }

    @Override
    public synchronized void shutdown() {
        if (!enabled) {
            super.shutdown();
            return;
        }
        try {
            logger.info("flushing...");
            bulk.flush();
            logger.info("waiting for outstanding bulk requests for maximum of 30 seconds...");
            int n = 30;
            while (outstandingBulkRequests.get() > 0 && n > 0) {
                Thread.sleep(1000L);
                n--;
            }
            logger.info("closing bulk...");
            bulk.close();
            logger.info("bulk closed, shutting down...");        
            super.shutdown();
            logger.info("shutting down completed");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
