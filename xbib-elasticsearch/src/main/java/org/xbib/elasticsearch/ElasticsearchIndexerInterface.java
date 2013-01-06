package org.xbib.elasticsearch;

import java.io.IOException;
import java.net.URI;
import org.elasticsearch.common.settings.Settings;

public interface ElasticsearchIndexerInterface {

    ElasticsearchIndexerInterface settings(Settings settings);

    ElasticsearchIndexerInterface newClient(URI uri, boolean forceNew);

    ElasticsearchIndexerInterface index(String index);

    String index();

    ElasticsearchIndexerInterface type(String type);

    String type();

    ElasticsearchIndexerInterface dateDetection(boolean dateDetection);
    
    boolean dateDetection();
    
    ElasticsearchIndexerInterface maxBulkActions(int bulkActions);
    
    ElasticsearchIndexerInterface maxConcurrentBulkRequests(int maxConcurentBulkRequests);

    ElasticsearchIndexerInterface index(String index, String type, String id, String source);

    ElasticsearchIndexerInterface delete(String index, String type, String id);

    ElasticsearchIndexerInterface flush();

    void shutdown();

}
