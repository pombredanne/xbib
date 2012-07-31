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
import java.net.URISyntaxException;
import org.elasticsearch.common.xcontent.XContentBuilder;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.xbib.elasticsearch.rdf.RDFBuilder;
import org.xbib.io.Identifiable;
import org.xbib.io.StringData;
import org.xbib.io.operator.CreateOperator;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;

/**
 * Abstract class for indexing resources to Elasticsearch
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public abstract class AbstractWrite<S extends Resource<?, ?, ?>, P extends Property, O extends Literal<?>>
        implements CreateOperator<ElasticsearchSession, Identifiable, Resource<S, P, O>> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractWrite.class.getName());
    private final RDFBuilder<S,P,O> rdfbuilder = new RDFBuilder();
    protected String index;
    protected String type;
    protected char delimiter;

    public AbstractWrite(String index, String type) {
        this(index, type, ':');
    }

    public AbstractWrite(String index, String type, char delimiter) {
        this.delimiter = delimiter;
        this.index = index;
        this.type = type;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void createIndex(ElasticsearchSession session) throws IOException {
        CreateIndex create = new CreateIndex();
        create.setIndex(index);
        create.setType(type);
        StringData data = new StringData("{\""+type+"\":{\"date_detection\":false}}");
        try {
            create.create(session, null, data);
        } catch (IndexAlreadyExistsException e) {
            logger.warn(e.getMessage());
        }
    }
    
    public String createId(Resource resource) {
        if (resource.getIdentifier() == null) {
            return null;
        }
        String id = resource.getIdentifier().getFragment();
        if (id == null) {
            id = resource.getIdentifier().toString();
        }
        return id;
    }

    @Override
    public abstract void write(ElasticsearchSession session, Resource<S, P, O> resource)
            throws IOException;

    @Override
    public abstract void create(ElasticsearchSession session, Identifiable identifiable, Resource<S, P, O> resource)
            throws IOException;

    @Override
    public abstract void flush(ElasticsearchSession session)
            throws IOException;

    /**
     * Build data for the Elasticsearch session.
     *
     * @param session
     * @param resource
     * @throws IOException
     */
    protected XContentBuilder build(Resource<S, P, O> resource) throws IOException {
        XContentBuilder builder = jsonBuilder();
        try {
            builder.startObject();
            rdfbuilder.build(builder, resource);
            builder.endObject();
        } catch (URISyntaxException e) {
            logger.warn(e.getMessage());
        }
        return builder;
    }

    protected char getDelimiter() {
        return delimiter;
    }

}
