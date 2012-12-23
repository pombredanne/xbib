package org.xbib.elasticsearch;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.net.URI;

public class ElasticsearchIndexerMock
        extends ElasticsearchMock
        implements ElasticsearchIndexerInterface {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchIndexerMock.class.getName());
    private String index;
    private String type;

    @Override
    public ElasticsearchIndexerMock newClient(URI uri, boolean force) {
        super.newClient(uri, force);
        return this;
    }

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
    public ElasticsearchIndexerMock setIndex(String index) {
        this.index = index;
        return this;
    }

    public String index() {
        return index;
    }

    @Override
    public ElasticsearchIndexerMock setType(String type) {
        this.type = type;
        return this;
    }

    public String type() {
        return type;
    }

    public ElasticsearchIndexerMock setBulkSize(int bulkSize) {
        return this;
    }

    public ElasticsearchIndexerMock setMaxActiveRequests(int maxActiveRequests) {
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

    public ElasticsearchIndexerInterface flush() {
        return this;
    }

}
