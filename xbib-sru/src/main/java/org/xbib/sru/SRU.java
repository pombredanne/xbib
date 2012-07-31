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
import java.util.HashMap;
import java.util.Map;

public interface SRU {
    
    String NS_URI = "http://www.loc.gov/zing/srw/";
    
    String NS_PREFIX = "srw";
    
    String VERSION_1_1 = "1.1";
    
    String VERSION_1_2 = "1.2";

    String SEARCH_RETRIEVE_COMMAND = "searchRetrieve";
    
    String SCAN_COMMAND = "scan";
    
    String OPERATION_PARAMETER = "operation";
    
    /**
     * The version of the request, and a statement by the client that 
     * it wants the response to be less than, or preferably equal to, 
     * that version
     */
    String VERSION_PARAMETER = "version";
    
    /**
     * Contains a query expressed in CQL to be processed by the server.
     */
    String QUERY_PARAMETER = "query";
    
    /**
     * The position within the sequence of matched records of the first 
     * record to be returned. The first position in the sequence is 1. 
     * The value supplied MUST be greater than 0. The default value 
     * if not supplied is 1.
     */
    String START_RECORD_PARAMETER = "startRecord";
    
    /**
     * The number of records requested to be returned. The value must be 
     * 0 or greater. Default value if not supplied is determined by the 
     * server. The server MAY return less than this number of records, 
     * for example if there are fewer matching records than requested, 
     * but MUST NOT return more than this number of records.
     */
    String MAXIMUM_RECORDS_PARAMETER = "maximumRecords";
    
    
   /**
     * Record Packing
     * In order that records which are not well formed do not break the
     * entire message, it is possible to request that they be transferred 
     * as a single string with the <, > and & characters escaped to their 
     * entity forms. Moreover some toolkits may not be able to distinguish 
     * record XML from the XML which forms the response. However, some 
     * clients may prefer that the records be transferred as XML in 
     * order to manipulate them directly with a stylesheet which renders 
     * the records and potentially also the user interface.
     * This distinction is made via the recordPacking parameter in the request. 
     * If the value of the parameter is 'string', then the server s
     * hould escape the record before transfering it. If the value is 
     * 'xml', then it should embed the XML directly into the response. 
     * Either way, the data is transfered within the 'recordData' field. 
     * If the server cannot comply with this packing request, then it 
     * must return a diagnostic.
     */    
    /**
     * A string to determine how the record should be escaped in the response. 
     * Defined values are 'string' and 'xml'. The default is 'xml'.
     */
    String RECORD_PACKING_PARAMETER = "recordPacking";
    
    /**
     * The schema in which the records MUST be returned. The value is the 
     * URI identifier for the schema or the short name for it published 
     * by the server. The default value if not supplied is determined 
     * by the server.
     * 
     * For version 1.1: If the recordXPath parameter is included, it is 
     * the abstract schema for purposes of evaluation by the XPath expression.
     */
    String RECORD_SCHEMA_PARAMETER = "recordSchema";
    
    /**
     * SRU 1.1 only
     * 
     * SRU can be used to retrieve any sort of XML records, regardless of 
     * size or complexity. Records in schemas such as TEI, EAD, SVG, X3D or 
     * OpenOffice's schema can be extremely long and complex.
     * 
     * Clients may request very specific sections of a record, rather than 
     * the entire record, for example to create a title list display. 
     * Even simple Dublin Core records may be considerably longer than 
     * the client requires, and to request the entire thing just to throw
     * most of it away seems wasteful of resources. A client may want to 
     * be able to page through a single record which is too long to 
     * display easily, but lacks the capability to do this segmentation 
     * itself. Another usage scenario would be if the client has some 
     * prior knowledge of the records and is only interested in certain 
     * sections, for example the trumpet section of an XML encoded 
     * musical score.
     * 
     * In order to enable clients to request only the parts of the record 
     * that it is immediately interested in, it may supply an XPath 
     * expression to be evaluated.
     * 
     * Semantics
     * 
     * The recordXPath field in the searchRetrieveRequest contains an 
     * XPath expression to be evaluated for each matching record. 
     * The results of this evaluation should be encoded using the 
     * schema described below.
     * 
     * The xpath expression is considered, unless otherwise namespaced, 
     * to be relative to the record schema. For example, if simple 
     * Dublin Core is given as the record schema, then the path 
     * '/dc/title/' is valid, but '/ead/eadheader/titlestmt/titleproper' 
     * is not, even if the record could be returned in EAD.
     * 
     * The '|' character may be used to separate multiple paths, as 
     * used in XSLT patterns. Equally, the xpath expression may match 
     * multiple nodes within the record. In either scenario, the response 
     * may contain multiple nodes within the nodeSet element in the 
     * response schema.
     * 
     * If the path supplied cannot be evaluated because it is invalid 
     * then the server may respond with a single fatal diagnostic. 
     * On the other hand, if the expression is valid but cannot be 
     * evaluated for a particular record, the server should respond 
     * with a surrogate diagnostic in the correct place within the 
     * result set. If the expression matches no nodes within the record, 
     * then an empty nodeSet may be returned rather than a diagnostic.
     * 
     * Servers may, at their discretion, refuse to process XPaths with 
     * any particular feature. For example, servers may refuse to 
     * process any XPath function, but accept regular paths of elements. 
     * Clients should be prepared for such a refusal via a diagnostic. 
     * Servers may also provide additional XPath functions which are not 
     * part of the standard library, for example a function which 
     * returns the element which matched the search query.
     * 
     * XPath Result Schema
     * 
     * The unique identifier for the XPath result schema is: 
     * info:srw/schema/1/xpath-1.0
     */
    String RECORD_XPATH_PARAMETER = "recordXPath";
    
    /**
     * The number of seconds for which the client requests that the result 
     * set created should be maintained. The server MAY choose not to fulfil 
     * this request, and may respond with a different number of seconds. 
     * If resultSetTTL is not supplied then the server will determine the 
     * value.
     */
    String RESULT_SET_TTL_PARAMETER = "resultSetTTL";
    
    String SORT_KEYS_PARAMETER = "sortKeys";
    
    /**
     * SRU messages may include additional information through a built 
     * in extension mechanism. Additional information may be included in 
     * the request (extraRequestData) or the response (extraResponseData). 
     * The client may send extraRequestData at will, and the server is 
     * free to honor or ignore it.
     * However, there is a no-unrequested-extraResponseData rule: The server 
     * may not send extraResponseData unless it has been requested 
     * (for example, via extraRequestData).
     * Sometimes a server may wish to send "helpful" information -- for 
     * example to identify ownership of the data/response, to show where 
     * the response came from, or to provide additional data which will 
     * help process the response -- information that the server thinks 
     * would be helpful to a client if the client understands it, and 
     * which the client could ignore if it does not understand it. 
     * But that would violate the no-unrequested-extraResponseData rule.
     * However, a client may signal that it will accept any data that 
     * the server sends (or it may signal that it will accept any data 
     * of a specific class). In that case when the server sends data 
     * (or data of the indicated class) that data is considered to be 
     * "requested" and so does not violate the 
     * no-unrequested-extraResponseData rule.
     * The method for the client to signal willingness to accept data 
     * is is by the inclusion of an accept extension in extraRequestData.
     */
    String EXTRA_REQUEST_DATA_PARAMETER = "extraRequestData";
    
    /**
     * The table below lists schemas in which records may be transferred. 
     * The list is not exhaustive. None are required to be supported, 
     * and any other schema (whether on the list below or not) may be 
     * supported in addition.
     * The Explain document lists the XML schemas for a given database in 
     * which records may be transferred. Every schemas is unambiguously 
     * identified by a URI and a server may assign a short name, which 
     * may or may not be the same as the short name listed in the table 
     * below (and may differ from the short name that another server assigns).
     */
    Map<String,URI> RECORD_SCHEMAS = new HashMap() {{
        put("dc",URI.create("info:srw/schema/1/dc-v1.1"));
        put("diag",URI.create("info:srw/schema/1/diagnostic-v1.1"));
        put("zeerex",URI.create("http://explain.z3950.org/dtd/2.0/"));
        put("mods",URI.create("info:srw/schema/1/mods-v3.4"));
        put("onix",URI.create("info:srw/schema/1/onix-v2.0"));
        put("marcxml",URI.create("info:srw/schema/1/marcxml-v1.1"));
        put("marcxchange",URI.create("info:/srw/schema/9/marcxchange"));
        put("mx",URI.create("info:/srw/schema/9/marcxchange"));
        put("ead",URI.create("info:srw/schema/1/ead-2002"));
        put("rec",URI.create("info:srw/schema/2/rec-1.0"));
        put("unimarcxml",URI.create("info:srw/schema/8/unimarcxml-v0.1"));
        put("dlxs-bib",URI.create("info:/srw/schema/10/dlxs-bib-v1.0"));
        put("dcx",URI.create("info:/srw/schema/1/dcx-v1.0"));
        put("pica-XML",URI.create("info:srw/schema/5/picaXML-v1.0"));
        put("mads",URI.create("info:srw/schema/1/mads-v1.0"));
        put("isohold",URI.create("info:srw/schema/5/iso20775-v1.0"));
        put("pam",URI.create("info:srw/schema/11/pam-v2.1"));
    }};
    
    Map<String,URI> RECORD_SCHEMA_NAMESPACES = new HashMap() {{
        put("mods",URI.create("http://www.loc.gov/mods/v3"));
        put("mx",URI.create("info:lc/xmlns/marcxchange-v1"));
        put("marc21",URI.create("http://www.loc.gov/MARC21/slim"));
    }};
    
    
    
}
