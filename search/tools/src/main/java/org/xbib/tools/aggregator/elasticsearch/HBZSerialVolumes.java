package org.xbib.tools.aggregator.elasticsearch;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.support.search.transport.TransportClientSearchSupport;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.constantScoreQuery;
import static org.elasticsearch.index.query.FilterBuilders.boolFilter;
import static org.elasticsearch.index.query.FilterBuilders.existsFilter;

public class HBZSerialVolumes {

    private final static Logger logger = LoggerFactory.getLogger(HBZSerialVolumes.class.getName());
    private static OptionSet options;
    private final static String lf = System.getProperty("line.separator");
    private TransportClientSearchSupport support;
    private String index;
    private String type;


    public HBZSerialVolumes(TransportClientSearchSupport support, String index, String type) {
        this.support = support;
        this.index = index;
        this.type = type;
    }

    private void execute() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Client client = support.client();
        TimeValue timeout = new TimeValue(60000);
        int size =100;
        FilterBuilder filterBuilder =
                boolFilter().must(existsFilter("xbib:identifierAuthorityISSN"))
                .must(existsFilter("xbib:identifierAuthorityISBN"));
        QueryBuilder queryBuilder =
                constantScoreQuery(filterBuilder);
        SearchResponse searchResponse = client.prepareSearch()
                .setIndices(index)
                .setTypes("type")
                .setSearchType(SearchType.SCAN)
                .setScroll(timeout)
                .setQuery(queryBuilder)
                .setSize(size)
                .execute().actionGet();
        do {
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(timeout)
                    .execute().actionGet();
            for (SearchHit hit : searchResponse.hits()) {
                logger.info("id = {}", hit.id());
                Object o = hit.getSource().get("dc:identifier.xbib:identifierAuthorityZDB");
                if (o != null) {
                    logger.info("ZDB ID = {}", o.toString());
                }
            }
        } while (searchResponse.hits().hits().length > 0);
    }

    public static void main(String[] args) {
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("elasticsearch").withRequiredArg().ofType(String.class).required();
                    accepts("index").withRequiredArg().ofType(String.class).required();
                    accepts("type").withRequiredArg().ofType(String.class).required();
                    accepts("shards").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("replica").withRequiredArg().ofType(Integer.class).defaultsTo(0);
                    accepts("path").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("1208zdblokutf8.mrc");
                    accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(1);
                    accepts("maxbulkactions").withRequiredArg().ofType(Integer.class).defaultsTo(100);
                    accepts("maxconcurrentbulkrequests").withRequiredArg().ofType(Integer.class).defaultsTo(10);
                    accepts("overwrite").withRequiredArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("elements").withRequiredArg().ofType(String.class).required().defaultsTo("marc");
                    accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("pipelines").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors());
                    accepts("buffersize").withRequiredArg().ofType(Integer.class).defaultsTo(8192);
                    accepts("detect").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                }
            };
            options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + HBZSerialVolumes.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --elasticsearch <uri>  Elasticesearch URI" + lf
                        + " --index <index>        Elasticsearch index name" + lf
                        + " --type <type>          Elasticsearch type name" + lf
                        + " --shards <n>           Elasticsearch number of shards" + lf
                        + " --replica <n>          Elasticsearch number of replica" + lf
                        + " --path <path>          a file path from where the input files are recursively collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: *.xml)" + lf
                        + " --threads <n>          the number of threads for import (optional, default: 1)"
                        + " --maxbulkactions <n>   the number of bulk actions per request (optional, default: 100)"
                        + " --maxconcurrentbulkrequests <n>the number of concurrent bulk requests (optional, default: 10)"
                        + " --elements <name>      element set (optional, default: marc)"
                        + " --mock <bool>          dry run of indexing (optional, default: false)"
                        + " --pipelines <n>        number of pipelines (optional, default: number of cpu cores)"
                        + " --buffersize <n>       buffer size in chars for reads (optional, default: 8192)"
                        + " --detect <bool>        detect unknown keys (optional, default: false)"
                );
                System.exit(1);
            }

            URI esURI = URI.create(options.valueOf("elasticsearch").toString());
            String index = options.valueOf("index").toString();
            String type = options.valueOf("type").toString();
            String shards = options.valueOf("shards").toString();
            String replica = options.valueOf("replica").toString();
            int maxbulkactions = (Integer) options.valueOf("maxbulkactions");
            int maxconcurrentbulkrequests = (Integer) options.valueOf("maxconcurrentbulkrequests");


            TransportClientSearchSupport es = new TransportClientSearchSupport()
                    .newClient(esURI);

            new HBZSerialVolumes(es, index, type);


        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
