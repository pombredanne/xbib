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
package org.xbib.sru.searchretrieve;

import org.xbib.sru.DefaultSRURequest;
import org.xbib.sru.SRURequest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class SearchRetrieveRequest extends DefaultSRURequest implements SRURequest {

    private String version = "1.2";

    private String recordPacking;

    private String recordSchema;

    private String query;

    private int recordNum;

    private int maxRecords;

    private int ttl;

    private String sortKeys;

    private String facetLimit;

    private String facetStart;

    private String facetSort;

    private String facetCount;

    private String data;

    private String encoding;

    private String path;

    private List<SearchRetrieveResponseListener> listeners = new ArrayList();

    protected SearchRetrieveRequest() {
    }

    @Override
    public SearchRetrieveRequest setURI(URI uri) {
        return (SearchRetrieveRequest)super.setURI(uri);
    }

    public SearchRetrieveRequest setPath(String path) {
        this.path = path;
        return this;
    }
    
    public String getPath() {
        return path;
    }
    
    /**
     * The version of the request, and a triple by the client that it
     * wants the response to be less than, or preferably equal to, that 
     * version.
     * @param version
     * @return 
     */
    public SearchRetrieveRequest setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public SearchRetrieveRequest setQuery(String query) {
        this.query = query;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public SearchRetrieveRequest setStartRecord(int recordNum) {
        this.recordNum = recordNum;
        return this;
    }

    public int getStartRecord() {
        return recordNum;
    }

    public SearchRetrieveRequest setMaximumRecords(int maxRecords) {
        this.maxRecords = maxRecords;
        return this;
    }

    public int getMaximumRecords() {
        return maxRecords;
    }

 
    public SearchRetrieveRequest setRecordPacking(String recordPacking) {
        this.recordPacking = recordPacking;
        return this;
    }

    public String getRecordPacking() {
        return recordPacking;
    }

    public SearchRetrieveRequest setRecordSchema(String recordSchema) {
        this.recordSchema = recordSchema;
        return this;
    }

    public String getRecordSchema() {
        return recordSchema;
    }

    public SearchRetrieveRequest setResultSetTTL(int ttl) {
        this.ttl = ttl;
        return this;
    }

    public int getResultSetTTL() {
        return ttl;
    }

    public SearchRetrieveRequest setSortKeys(String sortKeys) {
        this.sortKeys = sortKeys;
        return this;
    }

    public String getSortKeys() {
        return sortKeys;
    }

    public SearchRetrieveRequest setFacetLimit(String limitSpec) {
        this.facetLimit = limitSpec;
        return this;
    }

    public String getFacetLimit() {
        return facetLimit;
    }

    public SearchRetrieveRequest setFacetStart(String startSpec) {
        this.facetStart = startSpec;
        return this;
    }

    public String getFacetStart() {
        return facetStart;
    }

    public SearchRetrieveRequest setFacetCount(String countSpec) {
        this.facetCount = countSpec;
        return this;
    }

    public String getFacetCount() {
        return facetCount;
    }

    public SearchRetrieveRequest setFacetSort(String sortSpec) {
        this.facetSort = sortSpec;
        return this;
    }

    public String getFacetSort() {
        return facetSort;
    }

    public SearchRetrieveRequest setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public String getEncoding() {
        return encoding;
    }
    
    public SearchRetrieveRequest setExtraRequestData(String data) {
        this.data = data;
        return this;
    }

    public String getExtraRequestData() {
        return data;
    }

    public SearchRetrieveRequest addListener(SearchRetrieveResponseListener listener) {
        listeners.add(listener);
        return this;
    }

    public List<SearchRetrieveResponseListener> getListeners() {
        return listeners;
    }

}