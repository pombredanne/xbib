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
package org.xbib.sru;

import java.net.URI;

public class SearchRetrieve extends AbstractSRURequest {

    private String version;
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

    @Override
    public SearchRetrieve setURI(URI uri) {
        return (SearchRetrieve)super.setURI(uri);
    }
   
    @Override
    public SearchRetrieve setUsername(String username) {        
        return (SearchRetrieve)super.setUsername(username);
    }
    
    @Override
    public SearchRetrieve setPassword(String password) {
        return (SearchRetrieve)super.setPassword(password);
    }
    
    public SearchRetrieve setPath(String path) {
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
    public SearchRetrieve setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public SearchRetrieve setQuery(String query) {
        this.query = query;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public SearchRetrieve setStartRecord(int recordNum) {
        this.recordNum = recordNum;
        return this;
    }

    public int getStartRecord() {
        return recordNum;
    }

    public SearchRetrieve setMaximumRecords(int maxRecords) {
        this.maxRecords = maxRecords;
        return this;
    }

    public int getMaximumRecords() {
        return maxRecords;
    }

 
    public SearchRetrieve setRecordPacking(String recordPacking) {
        this.recordPacking = recordPacking;
        return this;
    }

    public String getRecordPacking() {
        return recordPacking;
    }

    public SearchRetrieve setRecordSchema(String recordSchema) {
        this.recordSchema = recordSchema;
        return this;
    }

    public String getRecordSchema() {
        return recordSchema;
    }

    public SearchRetrieve setResultSetTTL(int ttl) {
        this.ttl = ttl;
        return this;
    }

    public int getResultSetTTL() {
        return ttl;
    }

    public SearchRetrieve setSortKeys(String sortKeys) {
        this.sortKeys = sortKeys;
        return this;
    }

    public String getSortKeys() {
        return sortKeys;
    }

    public SearchRetrieve setFacetLimit(String limitSpec) {
        this.facetLimit = limitSpec;
        return this;
    }

    public String getFacetLimit() {
        return facetLimit;
    }

    public SearchRetrieve setFacetStart(String startSpec) {
        this.facetStart = startSpec;
        return this;
    }

    public String getFacetStart() {
        return facetStart;
    }

    public SearchRetrieve setFacetCount(String countSpec) {
        this.facetCount = countSpec;
        return this;
    }

    public String getFacetCount() {
        return facetCount;
    }

    public SearchRetrieve setFacetSort(String sortSpec) {
        this.facetSort = sortSpec;
        return this;
    }

    public String getFacetSort() {
        return facetSort;
    }

    public SearchRetrieve setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public String getEncoding() {
        return encoding;
    }
    
    public SearchRetrieve setExtraRequestData(String data) {
        this.data = data;
        return this;
    }

    public String getExtraRequestData() {
        return data;
    }

}
