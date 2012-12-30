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

import java.net.URI;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

/**
 * An Elasticsearch Data Access Object (DAO) for indexing
 */
public class ElasticsearchIndexer
        extends Elasticsearch
        implements ElasticsearchIndexerInterface {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchIndexer.class.getName());

    private boolean enabled;
    
    private BulkProcessor bulk;
    /**
     * The size of a bulkQueue request
     */
    private int bulkSize = 100;
    /**
     * The number of maximum active reqeuests
     */
    private int maxActiveRequests = 30;
    private String index;
    private String type;

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

    @Override
    public ElasticsearchIndexer newClient(URI uri, boolean forceNew) {
        super.newClient(uri, forceNew);
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {

            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                logger.info("sending bulk [{}]", executionId);
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                logger.info("bulk success [{}]", executionId);
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                logger.error("bulk error [{}]: {}", executionId, failure.getMessage());
                enabled = false;
            }
        };
        this.bulk = BulkProcessor.builder(client, listener)
                .setBulkActions(bulkSize)
                .setConcurrentRequests(maxActiveRequests)
                .build();
        this.enabled = true;
        return this;
    }

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
    public ElasticsearchIndexer setIndex(String index) {
        this.index = index;
        return this;
    }

    @Override
    public String index() {
        return index;
    }

    @Override
    public ElasticsearchIndexer setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public ElasticsearchIndexer setBulkSize(int bulkSize) {
        this.bulkSize = bulkSize;
        return this;
    }

    @Override
    public ElasticsearchIndexer setMaxActiveRequests(int maxActiveRequests) {
        this.maxActiveRequests = maxActiveRequests;
        return this;
    }

    public ElasticsearchIndexer newIndex(String mapping) throws NoNodeAvailableException {
        if (client == null) {
            return this;
        }
        client.admin().indices().create(new CreateIndexRequest(index)).actionGet();
        if (mapping != null) {
            client.admin().indices().putMapping(new PutMappingRequest()
                    .indices(new String[]{index})
                    .type(type)
                    .source(mapping)).actionGet();
        }
        return this;
    }

    public ElasticsearchIndexer deleteIndex() {
        if (client == null) {
            return this;
        }
        client.admin().indices().delete(new DeleteIndexRequest(index));
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
        if (client == null) {
            return this;
        }
        client.admin().indices().deleteMapping(new DeleteMappingRequest().indices(new String[]{index}).type(type));
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
            logger.error("bulk index failed: {}", e.getMessage(), e);
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
            logger.error("bulk delete failed: {}", e.getMessage(), e);
            enabled = false;
        }
        return this;
    }

    @Override
    public ElasticsearchIndexer flush() {
        if (enabled) {
            bulk.flush();
        }
        return this;
    }

    @Override
    public synchronized void shutdown() {
        logger.info("starting shutting down...");
        super.shutdown();
        bulk.close();
        logger.info("shutting down completed");
    }

}
