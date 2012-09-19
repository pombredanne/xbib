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
import java.io.OutputStream;
import org.xbib.io.InputStreamEmptyProcessor;
import org.xbib.io.InputStreamErrorProcessor;
import org.xbib.io.InputStreamProcessor;

public interface QueryResult<T> extends InputStreamProcessor, InputStreamEmptyProcessor, InputStreamErrorProcessor{
    
    /**
     * The format of the query result
     */
    enum Format { JSON, SMILE, YAML };
    
    /**
     * The index
     * @param index 
     */
    void setIndex(String... index);

    /**
     * The type
     * @param type 
     */
    void setType(String... type);
    
    /**
     * The document ID (for Get)
     * @param id 
     */
    void setId(String id);
    
    /**
     * Hit offset
     */
    void setFrom(int from);
    
    /**
     * Hit size
     * @param size 
     */
    void setSize(int size);
        
    /**
     * Set output stream
     * @param target 
     */
    void setOutputStream(OutputStream target);
    
    /**
     * Get output stream
     * @return 
     */
    OutputStream getOutputStream();
    
    /**
     * Search for result and output them in a format to the output stream
     * @param format
     * @param query
     * @throws IOException 
     */
    void search(T format, String query) throws IOException;

    /**
     * Search for result, process it to an input stream, see the processor interfaces.
     * @param format
     * @param query
     * @throws IOException 
     */
    void searchAndProcess(T format, String query) throws IOException;
}
