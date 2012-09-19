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

public abstract class AbstractQueryResultAction<T> implements QueryResult<T> {

    protected String[] index;
    protected String[] type;
    protected String id;
    protected int from;
    protected int size;
    private long timeout;
    private long tookInMillis;

    @Override
    public void setIndex(String... index) {
        this.index = index;
    }

    @Override
    public void setType(String... type) {
        this.type = type;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public void setFrom(int from) {
        this.from = from;
    }    
    
    public int getFrom() {
        return from;
    }
    
    @Override
    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
    
    public void setTookInMillis(long millis) {
        this.tookInMillis = millis;
    }
    
    public long getTookInMillis() {
        return this.tookInMillis;
    }

    public void setTimeout(long millis) {
        this.timeout = millis;
    }
    
    public long getTimeout() {
        return timeout;
    }
    
}
