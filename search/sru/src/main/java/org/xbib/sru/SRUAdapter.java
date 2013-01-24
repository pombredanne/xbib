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

import java.io.IOException;
import java.net.URI;
import org.xbib.sru.explain.Explain;
import org.xbib.xml.transform.StylesheetTransformer;

public interface SRUAdapter {
    
    /**
     * Get the URI of this adapter. The URI describes the SRU server.
     * @return the URI
     */
    URI getURI();

    /**
     * Connect to the SRU server.
     */
    void connect();
    
    /**
     * Disconnect from the SRU server.
     */
    void disconnect();

    /**
     * Set style sheet transformer.
     * @param transformer 
     */
    void setStylesheetTransformer(StylesheetTransformer transformer);
       
    /**
     * Execute SRU explain.
     * @param request
     * @param response
     * @throws Diagnostics
     * @throws IOException 
     */
    void explain(Explain request, ExplainResponse response) 
            throws Diagnostics, IOException;
    
    /**
     * Execute SRU scan.
     * @param request
     * @param response
     * @throws Diagnostics
     * @throws IOException 
     */
    void scan(Scan request, ScanResponse response) 
            throws Diagnostics, IOException;
    
    /**
     * Execute SRU searchRetrieve.
     * @param request
     * @param response
     * @throws Diagnostics
     * @throws IOException 
     */
    void searchRetrieve(SearchRetrieve request, SearchRetrieveResponse response) 
            throws Diagnostics, IOException;
    
    /**
     * Get SRU version. Valid versions are: 1.1, 1.2, 2.0
     * @return the SRU version
     */
    String getVersion();
    
    /**
     * Get SRU record schema.
     * @return the SRU record schema
     */
    String getRecordSchema();
    
    /**
     * Get SRU record packing.
     * @return the SRU record packing
     */
    String getRecordPacking();
    
    /**
     * Get encoding.
     * @return the encoding
     */
    String getEncoding();
    
    /**
     * Get style sheet name.
     * @return style sheet name
     */
    String getStylesheet();
    
}
