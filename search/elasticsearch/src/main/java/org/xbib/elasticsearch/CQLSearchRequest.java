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

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.xbib.elasticsearch.action.search.support.BasicRequest;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.query.cql.CQLParser;
import org.xbib.query.cql.elasticsearch.ESFilterGenerator;
import org.xbib.query.cql.elasticsearch.ESQueryGenerator;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Common Query Language request for Elasticsearch
 *
 */
public class CQLSearchRequest extends BasicRequest {

    private static final Logger logger = LoggerFactory.getLogger(CQLSearchRequest.class.getName());

    private String[] index;

    private String[] type;

    private String id;

    private String cqlQuery;

    private String cqlFilter;

    private String cqlFacetFilter;

    private String query;

    private ESQueryGenerator generator;

    private XContentBuilder filter;

    private XContentBuilder facets;

    private XContentBuilder facetfilter;

    public CQLSearchRequest newSearchRequest(SearchRequestBuilder searchRequestBuilder) {
        super.newSearchRequest(searchRequestBuilder);
        this.generator = new ESQueryGenerator();
        return this;
    }

    public CQLSearchRequest newGetRequest(GetRequestBuilder getRequestBuilder) {
        super.newGetRequest(getRequestBuilder);
        return this;
    }

    public CQLSearchRequest index(String index) {
        if (index != null && !"*".equals(index)) {
            this.index = new String[]{index};
        }
        return this;
    }

    public CQLSearchRequest index(String... index) {
        this.index = index;
        return this;
    }

    public String index() {
        return index[0];
    }

    public CQLSearchRequest type(String type) {
        if (type != null && !"*".equals(type)) {
            this.type = new String[]{type};
        }
        return this;
    }

    public CQLSearchRequest type(String... type) {
        this.type = type;
        return this;
    }

    public String type() {
        return type[0];
    }

    public CQLSearchRequest id(String id) {
        this.id = id;
        return this;
    }

    public String id() {
        return id;
    }

    public CQLSearchRequest from(int from) {
        if (searchRequestBuilder() != null) {
            searchRequestBuilder().setFrom(from);
        }
        generator.setFrom(from);
        return this;
    }

    public CQLSearchRequest size(int size) {
        if (searchRequestBuilder() != null) {
            searchRequestBuilder().setSize(size);
        }
        generator.setSize(size);
        return this;
    }

    /**
     * Translate SearchRetrieve facets to Elasticsearch
     * @param limit
     * @param sort
     * @param facetTypes
     * @return
     */
    public CQLSearchRequest facet(final String limit,
                                  final String sort,
                            final Map<String,String> facetTypes) {
        if (limit == null) {
            return this;
        }
        if (searchRequestBuilder() == null) {
            return this;
        }
        Map<String,Integer> facetMap = parseFacet(limit);
        String[] sortSpec = sort != null ? sort.split(",") : new String[] { "recordCount", "descending" };
        // all facets disabled?
        Integer globalSize = facetMap.get("*");
        if (globalSize == 0 ) {
            return this;
        }
        // count, term
        String order = "count";
        for (String s : sortSpec) {
            if ("recordCount".equals(s)){
                order = "count";
            } else if ("alphanumeric".equals(s)) {
                order = "term";
            } else if ("ascending".equals(s)) {
                order = "reverse_" + order;
            }
        }
        try {
            facets = jsonBuilder();
            facets.startObject();
            for (String index : facetMap.keySet()) {
                if ("*".equals(index)) {
                    continue;
                }
                String facetType = facetTypes != null && facetTypes.containsKey(index) ?
                        facetTypes.get(index) : "terms";
                Integer size = facetMap.get(index);
                facets.field(index)
                        .startObject()
                        .field(facetType).startObject()
                        .field("field", generator.getModel().getFieldOfIndex(index))
                        .field("size", size > 0 ? size : globalSize > 0 ? globalSize : Integer.MAX_VALUE)
                        .field("order", order)
                        .endObject();
                if (facetfilter != null) {
                    facets.rawField("facet_filter", facetfilter.bytes());
                }
                facets.endObject();
            }
            facets.endObject();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return this;
        }

        return this;
    }

    public CQLSearchRequest timeout(TimeValue timeout) {
        if (searchRequestBuilder() != null) {
            searchRequestBuilder().setTimeout(timeout);
        }
        return this;
    }


    public CQLSearchRequest query(String query) {
        this.query = query == null || query.trim().length() == 0 ? "{\"query\":{\"match_all\":{}}}" : query;
        return this;
    }

    public CQLSearchRequest filter(XContentBuilder filter) {
        if (filter == null) {
            return this;
        }
        this.filter = filter;
        return this;
    }

    public CQLSearchRequest cql(String query) throws IOException {
        if (query == null || query.trim().length() == 0) {
            from(0).size(10).query(null);
            return this;
        }
        this.cqlQuery = query;
        return this;
    }

    public CQLSearchRequest cqlFilter(String filter) throws IOException {
        if (filter == null || filter.trim().length() == 0) {
            return this;
        }
        this.cqlFilter = filter;
        CQLParser parser = new CQLParser(new StringReader(filter));
        parser.parse();
        ESFilterGenerator filterGenerator = new ESFilterGenerator();
        parser.getCQLQuery().accept(filterGenerator);
        this.filter = filterGenerator.getResult();
        return this;
    }

    public CQLSearchRequest cqlFacetFilter(String filter) throws IOException {
        if (filter == null) {
            return this;
        }
        this.cqlFacetFilter = filter;
        CQLParser parser = new CQLParser(new StringReader(filter));
        parser.parse();
        ESFilterGenerator filterGenerator = new ESFilterGenerator();
        parser.getCQLQuery().accept(filterGenerator);
        this.facetfilter = filterGenerator.getResult();
        return this;
    }


    public CQLSearchResponse executeSearch(Logger queryLogger)
            throws IOException {
        CQLSearchResponse response = new CQLSearchResponse();
        if (searchRequestBuilder() == null) {
            return response;
        }
        if (query == null) {
            query = createQuerySource();
        }
        if (query == null) {
            return response;
        }
        if (hasIndex(index)) {
            searchRequestBuilder().setIndices(fixIndexName(index));
        }
        if (hasType(type)) {
            searchRequestBuilder().setTypes(type);
        }
        long t0 = System.currentTimeMillis();
        response.setSearchResponse(searchRequestBuilder()
                .setExtraSource(query)
                .execute().actionGet());
        long t1 = System.currentTimeMillis();
        if (queryLogger != null) {
            queryLogger.info(" [{}] [total={}ms] [took={}ms] [hits={}] [cql={}] [cqlfilter={}] [cqlfacetfilter={}] [query={}] [filter={}] [facetfilter={}]",
                    formatIndexType(),
                    t1 - t0,
                    response.tookInMillis(),
                    response.totalHits(),
                    cqlQuery, cqlFilter, cqlFacetFilter,
                    query, filter, facetfilter);
        }
        return response;
    }

    public CQLSearchResponse executeGet(Logger queryLogger) throws IOException {
        CQLSearchResponse response = new CQLSearchResponse();
        long t0 = System.currentTimeMillis();
        response.setGetResponse(getRequestBuilder().execute().actionGet());
        long t1 = System.currentTimeMillis();
        if (queryLogger != null) {
            queryLogger.info(" get complete: {}/{}/{} [{}ms] {}",
                    index, type, getRequestBuilder().request().id(), (t1 - t0), response.exists());
        }
        return response;
    }

    private String createQuerySource()  {
        if (filter != null) {
            generator.setFilter(filter);
        }
        if (facets != null) {
            generator.setFacets(facets);
        }
        if (cqlQuery != null) {
            // all filters and facets are set, generate query at last
            try {
                CQLParser parser = new CQLParser(new StringReader(cqlQuery));
                parser.parse();
                parser.getCQLQuery().accept(generator);
                return generator.getSourceResult();
            } catch (IOException e) {
                //
            }
        }
        return null;
    }

    private boolean hasIndex(String[] s) {
        if (s == null) {
            return false;
        }
        if (s.length == 0) {
            return false;
        }
        if (s[0] == null) {
            return false;
        }
        return true;
    }

    private boolean hasType(String[] s) {
        if (s == null) {
            return false;
        }
        if (s.length == 0) {
            return false;
        }
        if (s[0] == null) {
            return false;
        }
        return true;
    }

    private String[] fixIndexName(String[] s) {
        if (s == null) {
            return new String[]{"*"};
        }
        if (s.length == 0) {
            return new String[]{"*"};
        }
        for (int i = 0; i < s.length; i++) {
            if (s[i] == null || s[i].length() == 0) {
                s[i] = "*";
            }
        }
        return s;
    }

    private String formatIndexType() {
        StringBuilder indexes = new StringBuilder();
        if (index != null) {
            for (String s : index) {
                if (s != null && s.length() > 0) {
                    if (indexes.length() > 0) {
                        indexes.append(',');
                    }
                    indexes.append(s);
                }
            }
        }
        if (indexes.length() == 0) {
            indexes.append('*');
        }
        StringBuilder types = new StringBuilder();
        if (type != null) {
            for (String s : type) {
                if (s != null && s.length() > 0) {
                    if (types.length() > 0) {
                        types.append(',');
                    }
                    types.append(s);
                }
            }
        }
        if (types.length() == 0) {
            types.append('*');
        }
        return indexes.append("/").append(types).toString();
    }

    private int defaultFacetlength = 10;

    private Map<String,Integer> parseFacet(String s) {
        Map<String,Integer> m = new HashMap();
        m.put("*", defaultFacetlength);
        if (s == null || s.length() == 0) {
            return m;
        }
        String[] params = s.split(",");
        for (String param : params) {
            int pos = param.indexOf(':');
            if (pos > 0) {
                // dc.subject -> 10
                int n = parseInt(param.substring(0, pos), defaultFacetlength);
                m.put(param.substring(pos+1), n);
            } else if (param.length() > 0) {
                int n =  parseInt(param, defaultFacetlength);
                m.put("*", n );
            }
        }
        return m;
    }

    private int parseInt(String s, int defaultValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public String toString() {
        return createQuerySource();
    }

}