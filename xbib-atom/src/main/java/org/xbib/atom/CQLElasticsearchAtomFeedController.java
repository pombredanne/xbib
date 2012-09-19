/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 * 
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses/
 *
 */
package org.xbib.atom;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.xbib.elasticsearch.xml.ES;
import org.xbib.elasticsearch.QueryResultAction;
import org.xbib.json.JsonXmlStreamer;
import org.xbib.json.JsonXmlValueMode;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.query.cql.elasticsearch.ElasticsearchCQLResultAction;

/**
 *  Atom feed controller for Elasticsearch CQL query.
 *  The results are wrapped up in an Atom feed format.
 */
public class CQLElasticsearchAtomFeedController extends ElasticsearchAtomFeedController {

    /** the logger */
    private final static Logger logger = LoggerFactory.getLogger(CQLElasticsearchAtomFeedController.class.getName());
    private final QueryResultAction processorInstance = new ElasticSearchAction();

    @Override
    protected QueryResultAction getAction() {
        return processorInstance;
    }

    class ElasticSearchAction extends ElasticsearchCQLResultAction {

        @Override
        public void process(InputStream in) throws IOException {
            JsonXmlStreamer jsonXml = new JsonXmlStreamer(JsonXmlValueMode.SKIP_EMPTY_VALUES);
            try {
                jsonXml.toXML(in, builder, new QName(ES.NS_URI, "result", ES.NS_PREFIX));
            } catch (XMLStreamException ex) {
                logger.error(ex.getMessage(), ex);
                throw new IOException(ex);
            }
        }
    }
}
