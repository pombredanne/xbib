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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.xbib.io.Identifiable;
import org.xbib.io.Session;
import org.xbib.rdf.Resource;

/**
 * Write resource to Elasticsearch
 * 
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class Write extends AbstractWrite {

    /** the logger */
    private static final Logger logger = Logger.getLogger(Write.class.getName());

    public Write(String index, String type) {
        this(index, type, ':');
    }

    public Write(String index, String type, char delimiter) {
        super(index, type, delimiter);
    }

    /**
     * Write resource to Elasticsearch
     * 
     * @param session
     * @param resource
     * @throws IOException
     */
    @Override
    public void write(ElasticsearchSession session, Resource resource)
            throws IOException {
        XContentBuilder builder = super.build(resource);
        if (!session.isOpen()) {
            throw new IOException("session not open");
        }
        try {
            IndexResponse response = session.getClient().prepareIndex().setIndex(index).setType(type)
                    .setId(createId(resource)).setSource(builder).execute().actionGet();
            logger.log(Level.FINE, "{0} indexed", response.getId());
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage() + " got exception for content = {0}" + builder.string(), e);
        }
    }

    @Override
    public void flush(ElasticsearchSession session) throws IOException {
    }

    @Override
    public void execute(Session session) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void create(ElasticsearchSession session, Identifiable identifiable, Resource resource) throws IOException {
        write(session, resource);
    }
}
