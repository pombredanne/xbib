package org.xbib.elasticsearch;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.xbib.elasticsearch.rdf.RDFBuilder;
import org.xbib.elements.output.ElementOutput;
import org.xbib.io.util.DateUtil;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.ResourceContext;

public class ElasticsearchIndexerMockDAO<C extends ResourceContext> 
    extends ElasticsearchMockDAO
    implements ElasticsearchIndexerInterface, ElementOutput<C> {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchIndexerMockDAO.class.getName());
    private static final int MAX_TOTAL_TIMEOUTS = 10;
    private static final AtomicInteger onGoingBulks = new AtomicInteger(0);
    private static final AtomicInteger bulkCounter = new AtomicInteger(0);
    private static final AtomicInteger counter = new AtomicInteger(0);
    private ThreadLocal<BulkRequestBuilder> currentBulk = new ThreadLocal();
    private final RDFBuilder rdfbuilder = new RDFBuilder();
    private int bulkSize = 100;
    private int maxActiveRequests = 30;
    private long millisBeforeContinue = 60000L;
    private int totalTimeouts;
    
    @Override
    public boolean enabled() {
        String enabled = System.getProperty(getClass().getName());
        return enabled == null || !"false".equalsIgnoreCase(enabled);
    }

    @Override
    public boolean output(C context) {
        try {
            Resource resource = context.resource();
            if (resource != null && !resource.isDeleted() && !resource.isEmpty()) {
                resource.property(resource.toPredicate("xbib:updated"), DateUtil.formatNow());
                write(resource);
                counter.incrementAndGet();
                return true;
            } else {
                logger.warn("no output of resource {}", resource);
                return false;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public long getCounter() {
        return counter.longValue();
    }

    @Override
    public ElasticsearchIndexerMockDAO newClient(URI uri, boolean force) {
        super.newClient(uri, force);
        return this;
    }
   
    public ElasticsearchIndexerMockDAO settings(Settings settings) {
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
    public ElasticsearchIndexerMockDAO setIndex(String index) {
        super.setIndex(index);
        return this;
    }
    
    @Override
    public ElasticsearchIndexerMockDAO setType(String type) {
        super.setType(type);
        return this;
    }
    
    public ElasticsearchIndexerMockDAO setBulkSize(int bulkSize) {
        this.bulkSize = bulkSize;
        return this; 
    }
    
    public ElasticsearchIndexerMockDAO setMaxActiveRequests(int maxActiveRequests) {
        this.maxActiveRequests = maxActiveRequests;
        return this;
    }
    
    public ElasticsearchIndexerMockDAO setMillisBeforeContinue(long millis) {
        this.millisBeforeContinue = millis;
        return this;        
    }

    public ElasticsearchIndexerMockDAO write(Resource resource) throws IOException {       
        if (currentBulk.get() == null) {
            currentBulk.set(new BulkRequestBuilder(null));
        }
        XContentBuilder builder = build(resource);
        if (resource.isDeleted()) {
            currentBulk.get().add(Requests.deleteRequest(index[0]).type(type[0]).id(createId(resource)));
        } else {
            currentBulk.get().add(Requests.indexRequest(index[0]).type(type[0]).id(createId(resource)).create(false).source(builder));
        }
        if (currentBulk.get().numberOfActions() >= bulkSize) {
            processBulk();
        }
        return this;
    }

    public ElasticsearchIndexerMockDAO flush() throws IOException {
        if (totalTimeouts > MAX_TOTAL_TIMEOUTS) {
            // waiting some minutes is much too long, do not wait any longer            
            throw new IOException("total flush() timeouts exceeded limit of + " + MAX_TOTAL_TIMEOUTS + ", aborting");
        }
        // submit the rest of the docs for this thread
        if (currentBulk.get() != null && currentBulk.get().numberOfActions() > 0) {
            processBulk();
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
        return this;
    }    
    
    protected  String createId(Resource resource) {
        if (resource.id() == null) {
            return null;
        }
        String id = resource.id().getFragment();
        if (id == null) {
            id = resource.id().toString();
        }
        return id;
    }
    
    protected XContentBuilder build(Resource resource) throws IOException {
        XContentBuilder builder = jsonBuilder();
        try {
            builder.startObject();
            rdfbuilder.build(builder, resource);
            builder.endObject();
        } catch (URISyntaxException e) {
            logger.warn(e.getMessage());
        }
        return builder;
    }
    
    protected void processBulk() {
        while (onGoingBulks.intValue() >= maxActiveRequests) {
            logger.info("waiting for {} active bulk requests", onGoingBulks);
        }
        int currentOnGoingBulks = onGoingBulks.incrementAndGet();
        final int numberOfActions = currentBulk.get().numberOfActions();
        logger.info("new bulk mock request ({} docs, {} requests currently active)", 
                numberOfActions, currentOnGoingBulks);
        currentBulk.set(new BulkRequestBuilder(null));
    }
    
}
