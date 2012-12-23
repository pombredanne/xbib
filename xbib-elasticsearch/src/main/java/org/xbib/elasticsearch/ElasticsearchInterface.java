package org.xbib.elasticsearch;

import java.net.URI;
import org.elasticsearch.common.settings.Settings;

public interface ElasticsearchInterface {

    ElasticsearchInterface settings(Settings settings);

    ElasticsearchInterface newClient(boolean force);

    ElasticsearchInterface newClient(URI uri, boolean force);

    ElasticsearchRequest newRequest();

    void shutdown();

}
