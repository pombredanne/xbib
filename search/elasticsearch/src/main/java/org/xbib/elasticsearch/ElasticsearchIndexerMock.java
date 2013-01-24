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
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;

public class ElasticsearchIndexerMock
        extends ElasticsearchMock
        implements ElasticsearchIndexerInterface {

    private String index;
    private String type;

    @Override
    public ElasticsearchIndexerMock newClient(URI uri) {
        super.newClient(uri);
        return this;
    }

    @Override
    public ElasticsearchIndexerMock settings(Settings settings) {
        this.settings = settings;
        return this;
    }

    @Override
    protected Settings initialSettings(URI uri) {
        return ImmutableSettings.settingsBuilder().put("cluster.name", findClusterName(uri))
                .put("client.transport.sniff", false)
                .put("transport.netty.connections_per_node.low", 0)
                .put("transport.netty.connections_per_node.med", 0)
                .put("transport.netty.connections_per_node.high", 10)
                .put("threadpool.search.type", "fixed")
                .put("threadpool.search.size", "1")
                .put("threadpool.get.type", "fixed")
                .put("threadpool.get.size", "1")
                .put("threadpool.index.type", "fixed")
                .put("threadpool.index.size", "10")
                .put("threadpool.bulk.type", "fixed")
                .put("threadpool.bulk.size", "10")
                .put("threadpool.refresh.type", "fixed")
                .put("threadpool.refresh.size", "1")
                .put("threadpool.percolate.type", "fixed")
                .put("threadpool.percolate.size", "1")
                .build();
    }

    @Override
    public ElasticsearchIndexerMock index(String index) {
        this.index = index;
        return this;
    }

    @Override
    public String index() {
        return index;
    }

    @Override
    public ElasticsearchIndexerMock type(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public ElasticsearchIndexerMock dateDetection(boolean dateDetection) {
        return this;
    }
    
    @Override    
    public boolean dateDetection() {
        return false;
    }
    
    @Override
    public ElasticsearchIndexerMock maxBulkActions(int maxBulkActions) {
        return this;
    }

    @Override
    public ElasticsearchIndexerMock maxConcurrentBulkRequests(int maxConcurrentRequests) {
        return this;
    }

    @Override
    public ElasticsearchIndexerMock index(String index, String type, String id, String source) {
        return this;
    }

    @Override
    public ElasticsearchIndexerMock delete(String index, String type, String id) {
        return this;
    }

    @Override
    public ElasticsearchIndexerMock flush() {
        return this;
    }

    @Override
    public ElasticsearchIndexerMock deleteIndex() {
        return this;
    }

    @Override
    public ElasticsearchIndexerMock newIndex() {
        return this;
    }
}
