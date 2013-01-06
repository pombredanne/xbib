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
    public ElasticsearchIndexerMock newClient(URI uri, boolean force) {
        super.newClient(uri, force);
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
    public ElasticsearchIndexerInterface index(String index, String type, String id, String source) {
        return this;
    }

    @Override
    public ElasticsearchIndexerInterface delete(String index, String type, String id) {
        return this;
    }

    @Override
    public ElasticsearchIndexerInterface flush() {
        return this;
    }

}
