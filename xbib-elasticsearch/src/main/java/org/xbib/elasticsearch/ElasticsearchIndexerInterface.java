package org.xbib.elasticsearch;

import java.io.IOException;
import java.net.URI;
import org.elasticsearch.common.settings.Settings;

public interface ElasticsearchIndexerInterface {

    ElasticsearchIndexerInterface settings(Settings settings);

    ElasticsearchIndexerInterface newClient(URI uri, boolean forceNew);

    ElasticsearchIndexerInterface setIndex(String index);

    String index();

    ElasticsearchIndexerInterface setType(String type);

    String type();

    ElasticsearchIndexerInterface setBulkSize(int bulkSize);
    
    ElasticsearchIndexerInterface setMaxActiveRequests(int maxActiveRequests);

    ElasticsearchIndexerInterface index(String index, String type, String id, String source);

    ElasticsearchIndexerInterface delete(String index, String type, String id);

    ElasticsearchIndexerInterface flush();

    void shutdown();

}
