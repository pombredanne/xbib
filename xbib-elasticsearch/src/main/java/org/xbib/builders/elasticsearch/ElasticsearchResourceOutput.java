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
package org.xbib.builders.elasticsearch;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;
import org.xbib.elasticsearch.AbstractWrite;
import org.xbib.elasticsearch.ElasticsearchConnection;
import org.xbib.elasticsearch.ElasticsearchSession;
import org.xbib.elasticsearch.Write;
import org.xbib.elements.output.DefaultElementOutput;
import org.xbib.io.Mode;
import org.xbib.io.util.DateUtil;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.ResourceContext;

public class ElasticsearchResourceOutput<C extends ResourceContext>
    extends DefaultElementOutput<C> {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchResourceOutput.class.getName());
    private ElasticsearchSession session;
    private final static AtomicLong counter = new AtomicLong(0L);
    private AbstractWrite operator;

    public void connect(String index, String type) throws IOException {
        this.session = ElasticsearchConnection.getInstance().createSession();
        this.operator = getOperator(index, type);
        connect();
    }

    public void connect(URI uri, String index, String type) throws IOException {
        this.session = ElasticsearchConnection.getInstance(uri).createSession();
        this.operator = getOperator(index, type);
        connect();
    }
    
    private void connect() {
        try {
            session.open(Mode.WRITE);
            if (!session.isOpen()) {
                logger.error("unable to open session {}", session);
            } else {
                logger.info("session {} created", session);
            }
            operator.createIndex(session);
        } catch (IOException e) {
            logger.warn("I/O exception while opening session, reason: {}",
                    e.getMessage());
        }
    }

    public void disconnect() throws IOException {
        operator.flush(session);
        
        session.close();
    }

    protected AbstractWrite getOperator(String index, String type) {
        return new Write(index, type);
    }
    
    @Override
    public void output(C context, Object info) {
        try {
            Resource resource = context.resource();
            if (session.isOpen() && resource != null && !resource.isDeleted() && !resource.isEmpty()) {
                resource.addProperty(resource.createPredicate("xbib:update"), DateUtil.formatNow());
                resource.addProperty(resource.createPredicate("xbib:info"), resource.createObject(info));              
                operator.write(session, resource);
                counter.incrementAndGet();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            try {
                session.close();
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
    
    @Override
    public long getCounter() {
        return counter.longValue();
    }

}
