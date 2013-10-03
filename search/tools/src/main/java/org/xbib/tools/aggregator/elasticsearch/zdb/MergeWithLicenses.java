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
package org.xbib.tools.aggregator.elasticsearch.zdb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.xbib.elasticsearch.support.bulk.transport.BulkClient;
import org.xbib.elasticsearch.support.search.transport.SearchClientSupport;
import org.xbib.common.xcontent.XContentBuilder;
import org.xbib.date.DateUtil;
import org.xbib.io.Streams;
import org.xbib.util.URIUtil;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.tools.aggregator.elasticsearch.WrappedSearchHit;
import org.xbib.tools.aggregator.elasticsearch.zdb.entities.Expression;
import org.xbib.tools.aggregator.elasticsearch.zdb.entities.Holding;
import org.xbib.tools.aggregator.elasticsearch.zdb.entities.License;
import org.xbib.tools.aggregator.elasticsearch.zdb.entities.Manifestation;
import org.xbib.tools.aggregator.elasticsearch.zdb.entities.Work;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;
import org.xbib.tools.util.ExceptionFormatter;
import org.xbib.tools.util.FormatUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicLong;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.xbib.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Merge ZDB and EZB
 *
 */
public class MergeWithLicenses {

    private final static Logger logger = LoggerFactory.getLogger(MergeWithLicenses.class.getName());

    private final static Logger missingRelationsLogger = LoggerFactory.getLogger("relations");

    private final static String lf = System.getProperty("line.separator");

    // the pump
    private int numPumps;
    private BlockingQueue<WrappedSearchHit> pumpQueue;
    private ExecutorService pumpService;
    private CountDownLatch pumpLatch;
    private Set<MergePump> pumps;

    private Client client;
    private BulkClient ingest;

    // Elasticsearch source index/types
    private String sourceTitleIndex;
    private String sourceTitleType;
    private String sourceHoldingsIndex;
    private String sourceHoldingsType;
    private String sourceLicenseIndex;
    private String sourceLicenseType;

    // Elastiscearch target index/type
    private String targetIndex;
    private String targetWorksType;
    private String targetExpressionsType;
    private String targetManifestationsType;
    private String targetHoldingsType;

    private int size;
    private long millis;

    private String identifier;

    // counters
    private final static AtomicLong countQueries = new AtomicLong(0L);
    private final static AtomicLong countHits = new AtomicLong(0L);
    private final static AtomicLong countWrites = new AtomicLong(0L);
    private final static AtomicLong countManifestations = new AtomicLong(0L);
    private final static AtomicLong countHoldings = new AtomicLong(0L);
    private final static AtomicLong countLicenses = new AtomicLong(0L);

    private Set<String> docs;

    public static void main(String[] args) {
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("source").withRequiredArg().ofType(String.class).required();
                    accepts("target").withRequiredArg().ofType(String.class).required();
                    accepts("shards").withOptionalArg().ofType(Integer.class).defaultsTo(4);
                    accepts("replica").withOptionalArg().ofType(Integer.class).defaultsTo(0);
                    accepts("mock").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
                    accepts("maxbulkactions").withRequiredArg().ofType(Integer.class).defaultsTo(1000);
                    accepts("maxconcurrentbulkrequests").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors() * 4);
                    accepts("pumps").withRequiredArg().ofType(Integer.class).defaultsTo(Runtime.getRuntime().availableProcessors() * 4);
                    accepts("size").withRequiredArg().ofType(Integer.class).defaultsTo(100);
                    accepts("millis").withRequiredArg().ofType(Long.class).defaultsTo(60000L);
                    accepts("id").withOptionalArg().ofType(String.class);
                }
            };
            OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + MergeWithLicenses.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --source <uri>         URI for connecting to the Elasticsearch source" + lf
                        + " --target <uri>         URI for connecting to Elasticsearch target" + lf
                        + " --shards <n>           number of shards" + lf
                        + " --replica <n>          number of replica" + lf
                        + " --mock <bool>          dry run of indexing (optional, default: false)"
                        + " --maxbulkactions <n>   the number of bulk actions per request (optional, default: 1000)"
                        + " --maxconcurrentbulkrequests <n>the number of concurrent bulk requests (optional, default: number of cpu core * 4)"
                        + " --pumps <n>            number of pumps (optional, default: number of cpu cores * 4)"
                        + " --size <n>             size for scan query result (optional, default: 1000)"
                        + " --millis <ms>          life time in milliseconds for scan query (optional, default: 60000)"
                        + " --id <n>               ZDB ID (optional, default is all ZDB IDs)"
                );
                System.exit(1);
            }

            URI sourceURI = URI.create(options.valueOf("source").toString());
            URI targetURI = URI.create(options.valueOf("target").toString());
            Integer maxBulkActions = (Integer) options.valueOf("maxbulkactions");
            Integer maxConcurrentBulkRequests = (Integer) options.valueOf("maxconcurrentbulkrequests");
            Integer pumps = (Integer) options.valueOf("pumps");
            Integer size = (Integer) options.valueOf("size");
            Long millis = (Long) options.valueOf("millis");
            String identifier = (String) options.valueOf("id");

            logger.info("connecting to search source {}...", sourceURI);

            SearchClientSupport search = new SearchClientSupport()
                    .newClient(sourceURI);

            logger.info("connecting to target index {} ...", targetURI);

            BulkClient ingest = new BulkClient()
                    .maxBulkActions(maxBulkActions)
                    .maxConcurrentBulkRequests(maxConcurrentBulkRequests)
                    .newClient(targetURI)
                    .waitForCluster();

            InputStream in = MergeWithLicenses.class.getResourceAsStream("mapping.json");
            StringWriter sw = new StringWriter();
            Streams.copy(new InputStreamReader(in), sw);
            ingest.mapping(sw.toString())
                    .newIndex(true);

            logger.info("starting aggregation: pumps={} size={}, millis={}", pumps, size, millis);
            long t0 = System.currentTimeMillis();
            new MergeWithLicenses(search, ingest, sourceURI, targetURI, pumps, size, millis, identifier)
                    .aggregate();
            long t1 = System.currentTimeMillis();

            long d = countWrites.get(); //number of documents written
            long bytes = ingest.getVolumeInBytes();
            double dps = d * 1000.0 / (double)(t1 - t0);
            double avg = bytes / (d + 1.0); // avoid div by zero
            double mbps = (bytes * 1000.0 / (double)(t1 - t0)) / (1024.0 * 1024.0) ;
            String t = TimeValue.timeValueMillis(t1 - t0).format();
            String byteSize = FormatUtil.convertFileSize(bytes);
            String avgSize = FormatUtil.convertFileSize(avg);
            NumberFormat formatter = NumberFormat.getNumberInstance();
            logger.info("Merging complete. {} docs written, {} = {} ms, {} = {} bytes, {} = {} avg size, {} dps, {} MB/s",
                    d,
                    t,
                    (t1-t0),
                    byteSize,
                    bytes,
                    avgSize,
                    formatter.format(avg),
                    formatter.format(dps),
                    formatter.format(mbps));

            double qps = countQueries.get() * 1000.0 / (double)(t1 - t0);
            logger.info("queries={} qps={} hits={} manifestations={} holdings={} licenses={}",
                    countQueries.get(),
                    formatter.format(qps),
                    countHits.get(),
                    countManifestations.get(),
                    countHoldings.get(),
                    countLicenses.get());


            ingest.shutdown();
            search.shutdown();


        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    private MergeWithLicenses(SearchClientSupport search, BulkClient ingest,
                             URI sourceURI, URI targetURI,
                             int numPumps, int size, long millis,
                             String identifier)
            throws UnsupportedEncodingException {
        this.client = search.client();
        this.ingest = ingest;

        Map<String,String> params = URIUtil.parseQueryString(sourceURI);

        // ZDB BIB
        this.sourceTitleIndex = params.get("bibIndex");
        this.sourceTitleType = params.get("bibType");
        // ZDB HOL
        this.sourceHoldingsIndex = params.get("holIndex");
        this.sourceHoldingsType = params.get("holType");
        // EZB licenses
        this.sourceLicenseIndex = params.get("licenseIndex");
        this.sourceLicenseType = params.get("licenseType");

        params = URIUtil.parseQueryString(targetURI);

        this.targetIndex = params.get("index");
        if (targetIndex == null) {
            targetIndex = sourceTitleIndex + "merge";
        }
        this.targetWorksType = "works";
        this.targetExpressionsType = "expressions";
        this.targetManifestationsType = "manifestations";
        this.targetHoldingsType = "holdings";

        this.size = size;
        this.millis = millis;

        this.docs = Collections.synchronizedSet(new HashSet());

        this.numPumps = numPumps;
        this.pumps = new HashSet();
        this.pumpQueue = new SynchronousQueue(true);
        this.pumpService = Executors.newFixedThreadPool(numPumps);
        this.pumpLatch = new CountDownLatch(numPumps);
        for (int i = 0; i < numPumps; i++) {
            MergePump mergePump = new MergePump(i);
            pumps.add(mergePump);
            pumpService.submit(mergePump);
        }

        this.identifier = identifier;
    }

    private MergeWithLicenses aggregate() {

        boolean failure = false;

        SearchRequestBuilder searchRequest = client.prepareSearch()
                .setSize(size)
                .setSearchType(SearchType.SCAN)
                .setScroll(TimeValue.timeValueMillis(millis));
        searchRequest.setIndices(sourceTitleIndex);
        if (sourceTitleType != null) {
            searchRequest.setTypes(sourceTitleType);
        }
        if (identifier != null) {
            // ZDB-ID
            logger.debug("adding term query for {}", identifier);
            searchRequest.setQuery(termQuery("IdentifierZDB.identifierZDB", identifier));
        }
        SearchResponse searchResponse = searchRequest.execute().actionGet();
        logger.debug("hits={}", searchResponse.getHits().getTotalHits());
        while (!failure && searchResponse.getScrollId() != null) {
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(millis))
                    .execute().actionGet();
            SearchHits hits = searchResponse.getHits();
            if (hits.getHits().length == 0) {
                break;
            }
            for (SearchHit hit : hits) {
                try {
                    WrappedSearchHit w = new WrappedSearchHit(hit);
                    pumpQueue.put(w);
                } catch (InterruptedException e) {
                    logger.error("interrupted");
                    Thread.currentThread().interrupt();
                    failure = true;
                } catch (Throwable e) {
                    logger.error("error while pumping, exiting", e);
                    logger.error(ExceptionFormatter.format(e));
                    Thread.currentThread().interrupt();
                }
            }
        }
        logger.info("terminating pumps");
        for (int i = 0; i < numPumps; i++) {
            try {
                // poison element
                pumpQueue.put(new WrappedSearchHit(null));
            } catch (InterruptedException e) {
                logger.error("interrupted");
            }
        }

        logger.info("waiting for pumps...");
        try {
            pumpLatch.await();
        } catch (InterruptedException e) {
            logger.error("interrupted");
        }

        logger.info("end of aggregation, failure = {}, shutting down...", failure);

        pumpService.shutdownNow();

        return this;
    }

    class MergePump implements Callable<Boolean> {

        private final Logger logger;
        private final ObjectMapper mapper;
        private final Queue<ClusterBuildContinuation> buildQueue;
        private Set<String> visited;
        private Set<Manifestation> cluster;

        public MergePump(int i) {
            this.buildQueue = new ConcurrentLinkedQueue();
            this.logger = LoggerFactory.getLogger(MergeWithLicenses.class.getName() + "-pump-" + i);
            this.mapper = new ObjectMapper();
            this.visited = Collections.synchronizedSet(new HashSet());
            this.cluster = Collections.synchronizedSet(new HashSet());
        }

        public Queue<ClusterBuildContinuation> getBuildQueue() {
            return buildQueue;
        }

        public Set<String> getVisited() {
            return visited;
        }

        public Set<Manifestation> getCluster() {
            return cluster;
        }

        @Override
        public Boolean call() throws Exception {
            Manifestation m = null;
            try {
                long count = 0;
                while (true) {
                    WrappedSearchHit t = pumpQueue.take();
                    if (t.hit() == null) {
                        logger.info("received 'end of pump' message");
                        break;
                    }
                    m = new Manifestation(mapper.readValue(t.hit().source(), Map.class));
                    if (filterForProcess(m)) {
                        process(m);
                    } else {
                        logger.debug("not processed: {}", m.targetID());
                    }
                    count++;
                    if (count % size == 0) {
                         logger.info("count={} queries={} hits={} manifestations={} holdings={} licences={}",
                                 count,
                                 countQueries.get(),
                                 countHits.get(),
                                 countManifestations.get(),
                                 countHoldings.get(),
                                 countLicenses.get());
                    }
                }
            } catch (InterruptedException e) {
                logger.warn("pump processing interrupted");
                Thread.currentThread().interrupt();
            } catch (Throwable e) {
                logger.error("error while processing pump {}, exiting", m.targetID(), e);
                logger.error(ExceptionFormatter.format(e));
                Thread.currentThread().interrupt();
            } finally {
                pumpLatch.countDown();
            }
            logger.info("pump processing terminating");
            return true;
        }

        private boolean filterForProcess(Manifestation manifestation) {
            return manifestation.isHead()
                    && !manifestation.isSupplement()
                    && !manifestation.isPart();
                    //&& !manifestation.hasPrintEdition(); // "print before online" rule
        }

        private void process(Manifestation manifestation) throws IOException {
            String docid = manifestation.id();
            if (docs.contains(docid)) {
                return;
            }
            docs.add(docid);

            cluster = Collections.synchronizedSet(new HashSet());
            cluster.add(manifestation);
            visited = Collections.synchronizedSet(new HashSet());
            visited.add(docid);

            buildCluster(manifestation, visited, cluster);
            countManifestations.addAndGet(cluster.size());

            Set<Work> leaders = electLeaders(cluster);

            if (logger.isDebugEnabled()) {
                logger.debug("elected {} leaders from {} cluster members: {}", leaders.size(), cluster.size(), leaders);
            }

            // assign all manifestations in the cluster to a leader
            int lastsize;
            do {
                lastsize = cluster.size();
                Set<Manifestation> set = new HashSet(cluster);
                for (Manifestation m : set) {
                    for (Work w : leaders) {
                        if (isConnected(m, w.getManifestations())) {
                            w.addManifestation(m);
                            cluster.remove(m);
                            break;
                        }
                    }
                }
            } while (!cluster.isEmpty() && cluster.size() < lastsize);
            if (!cluster.isEmpty()) {
                logger.error("cluster not empty?!? {}", cluster);
            }

            for (Work work : leaders) {
                Set<Holding> holdings = new HashSet();
                Set<License> licenses = new HashSet();
                work.setExpressions(electExpressions(work.getManifestations()));
                // search for all holdings of all manifestations in the cluster with one query
                Set<String> manifestationsIDs =  work.allIDs();
                if (logger.isDebugEnabled()) {
                    logger.debug("work {}: searching holdings for {} manifestations", work.targetID(), manifestationsIDs.size());
                }
                searchHoldings(manifestationsIDs, holdings);
                if (logger.isDebugEnabled()) {
                    logger.debug("work {}: found {} holdings ", work.targetID(), holdings.size());
                }
                countHoldings.addAndGet(holdings.size());
                // search for license documents
                Set<String> manifestationsTargetIDs = work.allTargetIDs();
                if (logger.isDebugEnabled()) {
                    logger.debug("work {}: searching licenses for {} manifestations", work.targetID(), manifestationsTargetIDs.size());
                }
                searchLicenses(manifestationsTargetIDs, licenses);
                if (logger.isDebugEnabled()) {
                    logger.debug("work {}: found {} licenses ", work.targetID(), licenses.size());
                }
                countLicenses.addAndGet(licenses.size());
                // output phase, write everything out
                if (logger.isDebugEnabled()) {
                    logger.debug("writing {} title '{}' with {} expressions", work.targetID(), work.title(), work.getExpressions().size());
                }
                Map<Integer, Set<Holding>> holdingsByDate = reorderHoldingsByDate(holdings);
                Map<Integer, Set<License>> licensesByDate = reorderLicensesByDate(licenses);
                // compute static boost for work
                Set<Integer> dates = new HashSet(holdingsByDate.keySet());
                dates.addAll(licensesByDate.keySet());
                double dateWeight = 1.0;
                for (Integer d : dates) {
                      dateWeight += d * 100.0;
                }
                dateWeight /= dates.size() / 100.0;
                int holdingsCount = holdingsByDate.values().size() + licensesByDate.values().size();
                double boost = 1.0 + dateWeight + holdingsCount;
                writeWork(work, holdings, licenses, boost);
                for (Expression expression : work.getExpressions().values()) {
                    writeExpression(work, expression);
                    for (Manifestation m : expression.getManifestations()) {
                        writeManifestation(work, expression, m, holdingsByDate, licensesByDate);
                    }
                }
                // orphaned manifestations, without expression...
                for (Manifestation m : cluster) {
                    writeManifestation(work, null, m, holdingsByDate, licensesByDate);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("writing {} holdings/licenses", holdings.size() + licenses.size());
                }
                for (Holding holding : holdings) {
                    writeHolding(holding);
                }
                for (License license : licenses) {
                    writeHolding(license);
                }
            }
        }

        private void buildCluster(Manifestation manifestation,
                                          Set<String> visited,
                                          Set<Manifestation> manifestations)
                throws IOException {
            // search for the manifestations we reference to and all manifestations that reference to us
            String id = manifestation.id();

            Set<String> neighbors = new HashSet();
            //neighborIDs(manifestation, relatedWorks, neighbors);  // ignore work neighbors
            neighborIDs(manifestation, relatedExpressions, neighbors);
            neighborIDs(manifestation, relatedManifestations, neighbors);
            neighbors.removeAll(visited); // do not search twice

            QueryBuilder queryBuilder = neighbors.isEmpty() ?
                    termQuery("_all", id) :
                    boolQuery().should(termQuery("_all", id))
                            .should(termsQuery("IdentifierDNB.identifierDNB", neighbors.toArray()));
            SearchRequestBuilder searchRequest = client.prepareSearch()
                    .setQuery(queryBuilder)
                    .setSize(size) // size is per shard!
                    .setSearchType(SearchType.SCAN)
                    .setScroll(TimeValue.timeValueMillis(millis));
            searchRequest.setIndices(sourceTitleIndex);
            if (sourceTitleType != null) {
                searchRequest.setTypes(sourceTitleType);
            }
            SearchResponse searchResponse = searchRequest.execute().actionGet();
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMillis(millis))
                   .execute().actionGet();
            countQueries.incrementAndGet();
            SearchHits hits = searchResponse.getHits();
            if (hits.getHits().length == 0) {
                return;
            }
            ClusterBuildContinuation start = new ClusterBuildContinuation(id, searchResponse,
                    0, neighbors, visited, manifestations);
            buildQueue.offer(start);
            while (!buildQueue.isEmpty()) {
                continueClusterBuild(buildQueue.poll());
            }
        }

        private void continueClusterBuild(ClusterBuildContinuation c) throws IOException {
            String id = c.id;
            SearchResponse searchResponse = c.searchResponse;
            int pos = c.pos;
            Set<String> neighbors = c.neighbors;
            Set<String> visited = c.visited;
            Set<Manifestation> manifestations = c.manifestations;
            SearchHits hits;
            boolean collided = false;
            do {
                hits = searchResponse.getHits();
                for (int i = pos; i < hits.getHits().length; i++ ) {
                    SearchHit hit = hits.getAt(i);
                    countHits.incrementAndGet();
                    Manifestation m = new Manifestation(mapper.readValue(hit.source(), Map.class));
                    // detect collision
                    collided = detectCollisionAndTransfer(m, c, i);
                    if (collided) {
                        break;
                    }
                    if (docs.contains(m.id())) {
                        continue;
                    }
                    if (neighbors.contains(m.id())) {
                        manifestations.add(m);
                        if (!visited.contains(m.id())) {
                            visited.add(m.id());
                            docs.add(m.id());
                            buildCluster(m, visited, manifestations);
                        }
                    } else {
                        String relation = checkRelationEntries(m, id);
                        if (relation != null) {
                            manifestations.add(m);
                            if (relatedManifestations.contains(relation)) {
                                if (!visited.contains(m.id())) {
                                    visited.add(m.id());
                                    docs.add(m.id());
                                    buildCluster(m, visited, manifestations);
                                }
                            } else if (relatedExpressions.contains(relation)) {
                                if (!visited.contains(m.id())) {
                                    visited.add(m.id());
                                    docs.add(m.id());
                                    buildCluster(m, visited, manifestations);
                                }
                            } else if (!relatedWorks.contains(relation)) {
                                missingRelationsLogger.warn("{} {}", m.targetID(), relation);
                            }
                        }
                    }
                }
                if (collided) {
                    break;
                }
                searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                        .setScroll(TimeValue.timeValueMillis(millis))
                        .execute().actionGet();
                countQueries.incrementAndGet();
                hits = searchResponse.getHits();
            } while (hits.getHits().length > 0);
        }

        private boolean detectCollisionAndTransfer(Manifestation manifestation,
                                        ClusterBuildContinuation c, int pos) {
            for (MergePump pump : pumps) {
                if (this == pump) {
                    continue;
                }
                if (pump.getCluster().contains(manifestation)) {
                    logger.warn("collision detected for {}", manifestation.targetID());
                    c.pos = pos;
                    pump.getBuildQueue().offer(c);
                    return true;
                }
            }
            return false;
        }

        private void searchHoldings(Set<String> ids, Set<Holding> holdings) throws IOException {
            if (ids == null || ids.isEmpty()) {
                return;
            }
            // split ids into portions of 1024 (default max clauses for Lucene)
            Object[] array = ids.toArray();
            for (int p = 0; p < array.length; p += 1024) {
                int begin = p;
                int end = p + 1024 > array.length ? array.length : p + 1024;
                Object[] subarray = Arrays.copyOfRange(array, begin, end);
                QueryBuilder queryBuilder = termsQuery("_all", subarray);
                // size is per shard
                SearchRequestBuilder searchRequest = client.prepareSearch()
                        .setQuery(queryBuilder)
                        .setSize(size)
                        .setSearchType(SearchType.SCAN)
                        .setScroll(TimeValue.timeValueMillis(millis));
                searchRequest.setIndices(sourceHoldingsIndex);
                if (sourceHoldingsType != null) {
                    searchRequest.setTypes(sourceHoldingsType);
                }
                SearchResponse searchResponse = searchRequest.execute().actionGet();
                while (true) {
                    searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                            .setScroll(TimeValue.timeValueMillis(millis))
                            .execute().actionGet();
                    SearchHits hits = searchResponse.getHits();
                    if (hits.getHits().length == 0) {
                        break;
                    }
                    countQueries.incrementAndGet();
                    for (SearchHit hit : hits) {
                        countHits.incrementAndGet();
                        Holding holding = new Holding(mapper.readValue(hit.source(), Map.class));
                        holdings.add(holding);
                    }
                }
            }
        }

        private void searchLicenses(Set<String> ids, Set<License> licenses) throws IOException {
            if (ids == null || ids.isEmpty()) {
                return;
            }
            // split ids into portions of 1024 (default max clauses for Lucene)
            Object[] array = ids.toArray();
            for (int p = 0; p < array.length; p += 1024) {
                int begin = p;
                int end = p + 1024 > array.length ? array.length : p + 1024;
                Object[] subarray = Arrays.copyOfRange(array, begin, end);
                QueryBuilder queryBuilder = termsQuery("ezb:zdbid", subarray);
                // size is per shard
                SearchRequestBuilder searchRequest = client.prepareSearch()
                        .setQuery(queryBuilder)
                        .setSize(size)
                        .setSearchType(SearchType.SCAN)
                        .setScroll(TimeValue.timeValueMillis(millis));
                searchRequest.setIndices(sourceLicenseIndex);
                if (sourceLicenseType != null) {
                    searchRequest.setTypes(sourceLicenseType);
                }
                SearchResponse searchResponse = searchRequest.execute().actionGet();
                while (true) {
                    searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                            .setScroll(TimeValue.timeValueMillis(millis))
                            .execute().actionGet();
                    SearchHits hits = searchResponse.getHits();
                    if (hits.getHits().length == 0) {
                        break;
                    }
                    countQueries.incrementAndGet();
                    for (SearchHit hit : hits) {
                        countHits.incrementAndGet();
                        License license = new License(mapper.readValue(hit.source(), Map.class));
                        licenses.add(license);
                    }
                }
            }
        }
    }

    /**
     * TODO split manifestion if there is a sub-manifestation
     * @param manifestation
     * @return
     */

    private List<Manifestation> splitManifestation(Manifestation manifestation) {
        List<Manifestation> list = new ArrayList();
        // microform-based split required?
        if ("microform".equals(manifestation.mediaType())) {
            list.add(new Manifestation(manifestation,
                    manifestation.id() + "_microform", // TODO
                    manifestation.targetID() + "_microform", // TODO
                    manifestation.contentType(),
                    manifestation.mediaType(),
                    manifestation.carrierType()));
            manifestation.contentType("text").mediaType("unmediated").carrierType("volume");
        }
        list.add(manifestation);
        return list;
    }

    private String checkRelationEntries(Manifestation manifestation, String id) {
        if (manifestation.id().equals(id)) {
            return null;
        }
        for (String entry : relationEntries) {
            String s = checkEntry(manifestation.map().get(entry), id);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    private String checkEntry(Object o, String id) {
        if (o != null) {
            if (!(o instanceof List)) {
                o = Arrays.asList(o);
            }
            for (Object s : (List) o) {
                Map<String, Object> entry = (Map<String, Object>) s;
                if (entry != null && id.equals(entry.get("identifierDNB"))) {
                    return (String)entry.get("relation");
                }
            }
        }
        return null;
    }

    private void neighborIDs(Manifestation manifestation, Set<String> relations, Set<String> ids) {
        for (String relationEntry : relationEntries) {
            Object o = manifestation.map().get(relationEntry);
            if (o == null) {
                continue;
            }
            if (!(o instanceof List)) {
                o = Arrays.asList(o);
            }
            for (Object p : (List) o) {
                Map<String, Object> entry = (Map<String, Object>) p;
                if (relations.contains(entry.get("relation"))) {
                    ids.add((String) entry.get("identifierDNB"));
                }
            }
        }
    }

    private Set<Work> electLeaders(Set<Manifestation> cluster) {
        Set<Work> leaders = new TreeSet(leaderComparator);
        // bring manifestations into order
        for (Manifestation manifestation : new HashSet<>(cluster)) {
            Iterator<Work> it = leaders.iterator();
            Work first = it.hasNext() ? it.next() : null;
            Work work = new Work(manifestation);
            if (manifestation.isHead()
                    && !manifestation.isSupplement()
                    && !manifestation.isPart()
                    && !manifestation.hasPrintEdition()
                    && (!isConnected(manifestation, leaders)
                        || (first != null && work.fromDate().compareTo(first.fromDate()) < 0 ))
                    ) {
                leaders.add(work);
                cluster.remove(manifestation);
            }
        }
        return leaders;
    }

    class LeaderComparator implements Comparator<Work> {

        @Override
        public int compare(Work e1, Work e2) {
            if (e1 == e2) {
                return 0;
            }
            return e1.fromDate().compareTo(e2.fromDate());
        }
    }

    private final LeaderComparator leaderComparator = new LeaderComparator();

    private boolean isConnected(Manifestation manifestation, Collection<Manifestation> manifestations) {
        if (manifestations.isEmpty()) {
            return false;
        }
        for (Manifestation m : manifestations) {
            if (isConnected(manifestation, m)) {
                return true;
            }
        }
        return false;
    }

    private boolean isConnected(Manifestation manifestation, Set<Work> manifestations) {
        if (manifestations.isEmpty()) {
            return false;
        }
        for (Manifestation m : manifestations) {
            if (isConnected(manifestation, m)) {
                return true;
            }
        }
        return false;
    }

    private boolean isConnected(Manifestation m1, Manifestation m2) {
        if (m1.id().equals(m2.id())) {
            return true;
        }
        String s = checkRelationEntries(m1, m2.id());
        if (s != null) {
            return true;
        }
        s = checkRelationEntries(m2, m1.id());
        if (s != null) {
            return true;
        }
        return false;
    }

    private Map<String, Expression> electExpressions(Set<Manifestation> manifestations) {
        Map<String, Expression> byLanguage = new HashMap();
        for (Manifestation manifestation : manifestations) {
            Expression expression = byLanguage.get(manifestation.language());
            if (expression == null) {
                expression = new Expression(manifestation.language(), manifestation);
                byLanguage.put(manifestation.language(), expression);
            }
            expression.addManifestation(manifestation);
        }
        return byLanguage;
    }

    private Map<Integer, Set<Holding>> reorderHoldingsByDate(Set<Holding> holdings) {
        Map<Integer, Set<Holding>> holdingsByDate = new TreeMap();
        for (Holding holding : holdings) {
            List<Integer> dates = null;
            // first, our dates!
            Object o = holding.map().get("dates");
            if (o != null) {
                if (!(o instanceof List)) {
                    o = Arrays.asList(o);
                }
                dates = new ArrayList();
                dates.addAll((List<Integer>)o);
            } else {
                o = holding.map().get("FormattedEnumerationAndChronology");
                if (o != null) {
                    if (!(o instanceof List)) {
                        o = Arrays.asList(o);
                    }
                    dates = parseDates((List<Map<String, Object>>) o);
                } else {
                    o = holding.map().get("NormalizedHolding");
                    if (o != null) {
                        if (!(o instanceof List)) {
                            o = Arrays.asList(o);
                        }
                        dates = parseDates((List<Map<String, Object>>) o);
                    }
                }
            }
            if (dates == null || dates.isEmpty()) {
                continue;
            }
            for (Integer date : dates) {
                Set<Holding> set = holdingsByDate.get(date);
                if (set == null) {
                    set = new HashSet();
                }
                set.add(holding);
                holdingsByDate.put(date, set);
            }
        }
        return holdingsByDate;
    }

    private final int currentYear = DateUtil.getYear();

    private List<Integer> parseDates(List<Map<String, Object>> groups) {
        int[] begin = new int[groups.size()];
        int[] end = new int[groups.size()];
        int max = 0;
        for (int i = 0; i < groups.size(); i++) {
            Map<String, Object> m = groups.get(i);
            Object o = m.get("movingwall");
            if (o != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("movingwall detected: {}", o);
                }
            }
            o = m.get("date");
            if (o == null) {
                continue;
            }
            if (!(o instanceof List)) {
                o = Arrays.asList(o);
            }
            for (String date : (List<String>) o) {
                if (date == null) {
                    continue;
                }
                date = date.replaceAll("[^\\d]", "");
                if (date.length() < 4) {
                    continue;
                }
                if (date.length() > 4) {
                    date = date.substring(0, 4);
                }
                int d = Integer.parseInt(date);
                if (d < 1500) {
                    d = 1500;
                }
                if (d > currentYear) {
                    d = currentYear;
                }
                String number = (String) m.get("sequenceNumber");
                if (number == null) {
                    continue;
                }
                int pos = number.indexOf('.');
                int p = pos >= 0 ? Integer.parseInt(number.substring(0, pos)) - 1 : 0;
                int q = pos >= 0 ? Integer.parseInt(number.substring(pos + 1, pos + 2)) : 0;
                if (q == 1) {
                    begin[p] = d;
                } else if (q == 2) {
                    end[p] = d;
                }
                if (p > max) {
                    max = p;
                }
            }
        }
        List<Integer> dates = new ArrayList();
        for (int i = 0; i < max+1; i++) {
            if (begin[i] > 0 && end[i] > 0) {
                for (int d = begin[i]; d <= end[i]; d++) {
                    dates.add(d);
                }
            } else if (begin[i] > 0) {
                // TODO
                dates.add(begin[i]);
            }
        }
        return dates;
    }

    private Map<Integer, Set<License>> reorderLicensesByDate(Set<License> licenses) {
        Map<Integer, Set<License>> licensesByDate = new TreeMap();
        for (License license : licenses) {
            for (Integer d : license.dates()) {
                Set<License> l = licensesByDate.get(d);
                if (l == null) {
                    l = new HashSet();
                }
                l.add(license);
                licensesByDate.put(d, l);
            }
        }
        return licensesByDate;
    }

    private void writeWork(Work work, Set<Holding> holdings, Set<License> licenses, double boost)
            throws IOException {
        if (work == null) {
            return;
        }
        XContentBuilder builder = jsonBuilder();
        int manifestationCount = 0;
        List<String> workIDs = new ArrayList();
        for (Manifestation m : work.getNeighbors()) {
            workIDs.add(m.targetID());
        }
        List<String> expressionIDs = new ArrayList();
        List<String> manifestationIDs = new ArrayList();
        for (Expression expr : work.getExpressions().values()) {
            expressionIDs.add(expr.targetID());
            manifestationCount += expr.getManifestations().size();
            for (Manifestation manifestation : expr.getManifestations()) {
                manifestationIDs.add(manifestation.targetID());
            }
        }
        // ISIL
        Set<String> isils = new HashSet();
        for (Holding holding : holdings) {
            if (holding.getISIL() != null) {
                isils.add(holding.getISIL());
            }
        }
        for (License license : licenses) {
            if (license.getISIL() != null) {
                isils.add(license.getISIL());
            }
        }
        builder.startObject()
                .field("_boost", boost)
                .field("preferredWorkTitle", work.title())
                .field("workCount", work.getNeighbors().size())
                .field("hasWorks", workIDs)
                .field("expressionCount", work.getExpressions().size())
                .field("hasExpressions", expressionIDs)
                .field("manifestationCount", manifestationCount)
                .field("hasManifestation", manifestationIDs)
                .field("holdingsCount", holdings.size())
                .field("hasISIL", isils);
        if (work.isSupplement()) {
            builder.field("isSupplement", work.isSupplement())
                    .field("supplementID", work.supplementTargetID());
        }
        builder.startArray("expressions");
        String date = "9999";
        for (Expression expr : work.getExpressions().values()) {
            builder.startObject()
                    .field("expressionID", expr.targetID())
                    .field("expressionKey", expr.getKey())
                    .field("fromDate", expr.fromDate())
                    .field("toDate", expr.toDate())
                    .field("manifestationCount", expr.getManifestations().size())
                    .startArray("manifestations");
            for (Manifestation manifestation : expr.getManifestations()) {
                builder.startObject()
                        .field("manifestationID", manifestation.targetID())
                        .field("title", getTitle(manifestation))
                        .field("publisher", manifestation.publisher())
                        .field("fromDate", manifestation.fromDate())
                        .field("toDate", manifestation.toDate())
                        .field("contentType", manifestation.contentType())
                        .field("mediaType", manifestation.mediaType())
                        .field("carrierType", manifestation.carrierType())
                        .field("identifiers", manifestation.getIdentifiers());
                if (manifestation.isSupplement()) {
                    builder.field("isSupplement", manifestation.isSupplement())
                            .field("supplementID", manifestation.supplementTargetID());
                }
                builder.endObject();
                String d = manifestation.fromDate();
                if (date.compareTo(d) > 0) {
                    date = d;
                }
            }
            builder.endArray();
            builder.endObject();
        }
        builder.endArray();
        if (!"9999".equals(date)) {
            builder.field("firstDate", Integer.parseInt(date));
        }
        builder.field("key", work.getUniqueIdentifier());
        builder.endObject();
        if (logger.isDebugEnabled()) {
            logger.debug("writing work {}", builder.string());
        }
        ingest.indexDocument(targetIndex,
                targetWorksType,
                work.targetID(),
                builder.string());
        countWrites.incrementAndGet();
    }

    private void writeExpression(Work work, Expression expression) throws IOException {
        if (work == null) {
            return;
        }
        XContentBuilder builder = jsonBuilder();
        builder.startObject()
                .field("language", expression.language())
                .field("expressionKey", expression.getKey())
                .field("hasWork", work.targetID())
                .field("manifestationCount", expression.getManifestations().size());
        builder.startArray("manifestations");
        String firstDate = "9999";
        for (Manifestation manifestation : expression.getManifestations()) {
            builder.startObject()
                    .field("manifestationID", manifestation.targetID())
                    .field("title", getTitle(manifestation))
                    .field("publisher", manifestation.publisher())
                    .field("fromDate", manifestation.fromDate())
                    .field("toDate", manifestation.toDate())
                    .field("contentType", manifestation.contentType())
                    .field("mediaType", manifestation.mediaType())
                    .field("carrierType", manifestation.carrierType())
                    .field("identifiers", manifestation.getIdentifiers());
            if (manifestation.isSupplement()) {
                builder.field("isSupplement", manifestation.isSupplement())
                        .field("supplementID", manifestation.supplementTargetID());
            }
            builder.endObject();
            String d = manifestation.fromDate();
            if (firstDate.compareTo(d) > 0) {
                firstDate = d;
            }
        }
        builder.endArray();
        if (!"9999".equals(firstDate)) {
            builder.field("firstDate", Integer.parseInt(firstDate));
        }
        builder.field("key", work.getUniqueIdentifier());
        builder.endObject();
        if (logger.isDebugEnabled()) {
            logger.debug("writing expression {}", builder.string());
        }
        ingest.indexDocument(targetIndex,
                targetExpressionsType,
                expression.targetID(),
                builder.string());
        countWrites.incrementAndGet();
    }

    private void writeManifestation(Work work, Expression expression, Manifestation manifestation,
                                    Map<Integer, Set<Holding>> holdingsByDate,
                                    Map<Integer, Set<License>> licensesByDate)
         throws IOException{
        // add up all the dates from holdings and licenses
        Set<Integer> dates = new HashSet(holdingsByDate.keySet());
        dates.addAll(licensesByDate.keySet());
        if (dates.isEmpty()) {
            logger.warn("only unspecified dates for manifestation {}", manifestation.targetID());
            return;
        }
        for (Integer date : dates) {
            // filter out holdings for this date
            Set<Holding> dateHoldings = new HashSet();
            if (holdingsByDate.get(date) != null) {
                for (Holding holding : holdingsByDate.get(date)) {
                    // only for this manifestation
                    if (manifestation.id().equals(holding.parent())) {
                        dateHoldings.add(holding);
                    }
                }
            }
            // filter out licenses for this date
            Set<License> dateLicenses = new HashSet();
            if (licensesByDate.get(date) != null) {
                for (License license : licensesByDate.get(date)) {
                    // only for this manifestation
                    if (manifestation.targetID().equals(license.parent())) {
                        dateLicenses.add(license);
                    }
                }
            }
            writeVolume(work, expression, manifestation, date, dateHoldings, dateLicenses);
        }
    }

    private void writeVolume(Work work, Expression expression, Manifestation manifestation,
                             Integer date,
                             Set<Holding> holdings,
                             Set<License> licenses)
            throws IOException {
        if (work == null) {
            return;
        }
        XContentBuilder builder = jsonBuilder();
        builder.startObject()
                .field("preferredTitle", manifestation.title())
                .field("hasWork", work.targetID())
                .field("title", getTitle(manifestation))
                .field("publisher", manifestation.publisher())
                .field("date", date)
                .field("contentType", manifestation.contentType())
                .field("mediaType", manifestation.mediaType())
                .field("carrierType", manifestation.carrierType())
                .field("identifiers", manifestation.getIdentifiers())
                .field("holdingsCount", holdings.size())
                .field("licenseCount", licenses.size());
        if (expression != null) {
            builder.field("hasExpression", expression.targetID());
        }
        if (manifestation.isSupplement()) {
            builder.field("isSupplement", manifestation.isSupplement())
                    .field("supplementID", manifestation.supplementTargetID());
        }
        Map<String,List<Holding>> services = new HashMap();
        for (License license : licenses) {
            if (license.getISIL() == null) {
                continue;
            }
            List<Holding> list = services.get(license.getISIL());
            if (list == null) {
                list = new ArrayList();
            }
            list.add(license);
            services.put(license.getISIL(), list);
        }
        for (Holding holding : holdings) {
            if (holding.getISIL() == null) {
                continue;
            }
            List<Holding> list = services.get(holding.getISIL());
            if (list == null) {
                list = new ArrayList();
            }
            list.add(holding);
            services.put(holding.getISIL(), list);
        }
        builder.startArray("services");
        for (Map.Entry<String,List<Holding>> me : services.entrySet()) {
            builder.startObject()
                    .field("isil", me.getKey())
                    .startArray("service");
            for (Holding holding : me.getValue()) {
                builder.startObject()
                        .field("mediaType", holding.mediaType())
                        .field("hasHolding", holding.id())
                        .endObject();
            }
            builder.endArray().endObject();
        }
        builder.endArray();

        String id = new StringBuilder()
                .append(manifestation.getUniqueIdentifier()).append('-').append(date)
                .toString();
        if (logger.isDebugEnabled()) {
            logger.debug("writing volume {}", builder.string());
        }
        ingest.indexDocument(targetIndex,
                targetManifestationsType,
                id,
                builder.string());
        countWrites.incrementAndGet();
    }

    private void writeHolding(Holding holding) throws IOException {
        XContentBuilder builder = jsonBuilder();
        builder.startObject()
                .field("isil", holding.getISIL())
                .field("subisil", holding.getSubISIL())
                .field("parent", holding.parent())
                .field("mediaType", holding.mediaType())
                .field("info", holding.info())
                .endObject();
        if (logger.isDebugEnabled()) {
            logger.debug("writing holding {}", builder.string());
        }
        ingest.indexDocument(targetIndex,
                targetHoldingsType,
                holding.id(),
                builder.string());
        countWrites.incrementAndGet();
    }

    private Map<String,Object> getTitle(Manifestation manifestation) {
        Map<String, Object> m = (Map<String, Object>) manifestation.map().get("TitleStatement");
        if (m != null) {
            String titleMedium = (String) m.get("titleMedium");
            if ("[Elektronische Ressource]".equals(titleMedium)) {
                m.remove("titleMedium");
            }
        }
        return m;
    }

    class ManifestationIdComparator implements Comparator<Manifestation> {

        @Override
        public int compare(Manifestation e1, Manifestation e2) {
            if (e1 == e2) {
                return 0;
            }
            return e1.id().compareTo(e2.id());
        }
    }

    private final ManifestationIdComparator idComparator = new ManifestationIdComparator();

    private final String[] relationEntries = new String[]{
            "PrecedingEntry",
            "SucceedingEntry",
            "OtherEditionEntry",
            "OtherRelationshipEntry",
            "SupplementSpecialIssueEntry"
            //"SupplementParentEntry", weglassen: verbindet Titel auch mit (Datenbank)werken über "In" = "isPartOf" --> Work/Work
    };

    private final Set<String> relatedWorks = new HashSet<String>() {{
        add("hasPart");
        add("isPartOf");
        add("hasSupplement");
        add("isSupplementOf");
    }};


    private final Set<String> relatedExpressions = new HashSet<String>() {{
        add("hasLanguageEdition");
        add("hasTranslation");
        add("isLanguageEditionOf");
        add("isTranslationOf");
    }};

    private final Set<String> relatedManifestations = new HashSet<String>() {{
        add("hasOriginalEdition");
        add("hasPrintEdition");
        add("hasOnlineEdition");
        add("hasBrailleEdition");
        add("hasDVDEdition");
        add("hasCDEdition");
        add("hasDiskEdition");
        add("hasMicroformEdition");
        add("hasDigitizedEdition");
        add("hasSpatialEdition");
        add("hasTemporalEdition");
        add("hasPartialEdition");
        add("hasLocalEdition");
        add("hasAdditionalEdition");
        add("hasAlternativeEdition");
        add("hasDerivedEdition");
        add("hasHardcoverEdition");
        add("hasManuscriptEdition");
        add("hasBoxedEdition");
        add("hasReproduction");
        add("hasSummary");
        // other relation direction
        add("isOriginalEditionOf");
        add("isPrintEditionOf");
        add("isOnlineEditionOf");
        add("isBrailleEditionOf");
        add("isDVDEditionOf");
        add("isCDEditionOf");
        add("isDiskEditionOf");
        add("isMicroformEditionOf");
        add("isDigitizedEditionOf");
        add("isSpatialEditionOf");
        add("isTemporalEditionOf");
        add("isPartialEditionOf");
        add("isAdditionalEditionOf");
        add("isHardcoverEditionOf");
        add("isManuscriptEditionOf");
        add("isBoxedEditionOf");
        add("isReproductionOf");
        add("isSummaryOf");
        // chronological entries
        add("precededBy");
        add("succeededBy");
    }};

    class ClusterBuildContinuation {
        String id;
        SearchResponse searchResponse;
        int pos;
        Set<String> neighbors;
        Set<String> visited;
        Set<Manifestation> manifestations;

        ClusterBuildContinuation(String id,
                                 SearchResponse searchResponse,
                                 int pos,
                                 Set<String> neighbors,
                                 Set<String> visited,
                                 Set<Manifestation> manifestations) {
            this.id = id;
            this.searchResponse = searchResponse;
            this.pos = pos;
            this.neighbors = neighbors;
            this.visited = visited;
            this.manifestations = manifestations;
        }
    }

}
