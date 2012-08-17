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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

public class QueryResultAction extends AbstractQueryResultAction {

    private static final Logger logger = LoggerFactory.getLogger(QueryResultAction.class.getName());
    private Logger queryLogger;
    private ElasticsearchSession session;
    private OutputStream out;
    private String filter;
    private String facets;

    public void setSession(ElasticsearchSession session) throws IOException {
        this.session = session;
        session.setQueryLogger(queryLogger);
        setTimeout(30000L); // default time out
    }

    @Override
    public void setTarget(OutputStream target) {
        this.out = target;
    }

    @Override
    public OutputStream getTarget() {
        return out;
    }

    @Override
    public void search(final String query) throws IOException {
        SearchResponse response = performQuery(query);
        if (response != null) {
            setTookInMillis(response.getTookInMillis());
            createJSONStream(response, out);
        } else {
            out.write(jsonErrorMessage("no response"));
        }
    }

    public void setQueryLogger(Logger queryLogger) {
        this.queryLogger = queryLogger;
    }

    @Override
    public void searchAndProcess(final String query) throws IOException {
        SearchResponse response = performQuery(query);
        if (response != null) {
            setTookInMillis(response.getTookInMillis());
            pipeJSONStream(response);
        } else {
            processError(jsonErrorMessageStream("no response"));
        }
    }

    public void get(String index, String type, String id) throws IOException {
        long t0 = System.currentTimeMillis();
        byte[] message = jsonErrorMessage("no response");
        try {
            GetResponse response = session.getClient().prepareGet(index, type, id).execute().actionGet();
            if (response != null) {
                // found something?
                if (!response.exists() || response.isSourceEmpty()) {
                    message = jsonEmptyMessage("not found");
                } else {
                    message = response.source();
                }
            }
        } catch (NoNodeAvailableException e) {
            logger.error(e.getMessage(), e);
        } finally {
            out.write(message);
        }
        long t1 = System.currentTimeMillis();
        logger.info("get complete: {}/{}/{} [{}ms]", index, type, id, (t1 - t0));
    }

    public void getAndProcess(String index, String type, String id) throws IOException {
        long t0 = System.currentTimeMillis();
        try {
            GetResponse response = session.getClient().prepareGet(index, type, id).execute().actionGet();
            if (response != null) {
                if (!response.exists() || response.isSourceEmpty()) {
                    processEmpty(jsonEmptyMessageStream("not found"));
                } else {
                    process(new ByteArrayInputStream(response.source()));
                }
            } else {
                processError(jsonErrorMessageStream("no response"));
            }
        } catch (NoNodeAvailableException e) {
            logger.error(e.getMessage(), e);
            processError(jsonErrorMessageStream(e.getMessage()));
        }
        long t1 = System.currentTimeMillis();
        logger.info("get(process) complete: {}/{}/{} [{}ms]", index, type, id, (t1 - t0));
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setFacets(String facets) {
        this.facets = facets;
    }

    @Override
    public void process(InputStream in) throws IOException {
        if (out == null) {
            return;
        }
        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }

    @Override
    public void processEmpty(InputStream in) throws IOException {
        process(in);
    }

    @Override
    public void processError(InputStream in) throws IOException {
        process(in);
    }

    protected String buildQuery(SearchRequestBuilder builder, String query) throws IOException {
        String q = query == null ? "{\"match_all\":{}}" : query;
        builder.setFrom(from).setSize(size).setExtraSource(q);
        if (filter != null) {
            builder.setFilter(filter);
        }
        if (facets != null) {
            builder.setFacets(facets.getBytes());
        }
        return q;
    }

    protected SearchResponse performQuery(final String query) throws IOException {
        long t0 = System.currentTimeMillis();

        SearchRequestBuilder request = session.getClient().prepareSearch();
        String translated = buildQuery(request, query);
        request.setTimeout(new TimeValue(getTimeout()));

        // index/type
        StringBuilder indexes = new StringBuilder();
        boolean hasIndex = false;
        if (index != null) {
            for (String s : index) {
                hasIndex |= s != null && s.length() > 0;
                if (indexes.length() > 0) {
                    indexes.append(',');
                }
                indexes.append(s);
            }
        }
        if (hasIndex) {
            request.setIndices(index);
        } else {
            indexes.append('*');
        }
        boolean hasType = false;
        StringBuilder types = new StringBuilder();
        if (type != null) {
            for (String s : type) {
                hasType |= s != null && s.length() > 0;
                if (types.length() > 0) {
                    types.append(',');
                }
                types.append(s);
            }
        }
        if (hasType) {
            request.setTypes(type);
        } else {
            types.append('*');
        }
        StringBuilder address = new StringBuilder().append(indexes).append('/').append(types);
        try {
            SearchResponse response = request.execute().actionGet();
            long t1 = System.currentTimeMillis();
            long searchTime = response.tookInMillis();
            long hits = response.getHits().getTotalHits();
            session.getQueryLogger().info("[{}] [{}ms] [{}ms] [{}] [{}] [{}]",
                    address, t1 - t0, searchTime, hits, query, translated);
            return response;
        } catch (NoNodeAvailableException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    protected void createJSONStream(final SearchResponse response, OutputStream out) throws IOException {
        XContentBuilder builder = new XContentBuilder(JsonXContent.jsonXContent, out);
        builder.startObject();
        response.toXContent(builder, ToXContent.EMPTY_PARAMS);
        builder.endObject();
        builder.close();
    }

    protected void pipeJSONStream(final SearchResponse response) throws IOException {
        // choose what pipe we use
        final boolean notfound = response.getHits().totalHits() == 0L;
        final boolean error = response.failedShards() > 0 || response.isTimedOut();

        // @todo do we really need a ByteOutputStream? 
        // we just use a "simple" memory pipe = byte array in, byte array out
        // all must fit into memory :-(
        // PipedInputStream/PipedOutputStream breaks servlet filter stylesheets (they use threads)

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        createJSONStream(response, bout);
        bout.close();
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());

        if (error) {
            processError(bin);
        } else if (notfound) {
            processEmpty(bin);
        } else {
            process(bin);
        }
    }

    private static byte[] jsonEmptyMessage(String message) {
        return ("{\"error\":404,\"message\":\"" + message + "\"}").getBytes();
    }

    private static byte[] jsonErrorMessage(String message) {
        return ("{\"error\":500,\"message\":\"" + message + "\"}").getBytes();
    }

    private static InputStream jsonEmptyMessageStream(String message) {
        return new ByteArrayInputStream(jsonEmptyMessage(message));
    }

    private static InputStream jsonErrorMessageStream(String message) {
        return new ByteArrayInputStream(jsonErrorMessage(message));
    }
}
