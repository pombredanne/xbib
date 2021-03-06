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
package org.xbib.atom.elasticsearch;

import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.provider.managed.FeedConfiguration;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.testng.annotations.Test;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

/**
 * BulkIndexerTest CQL Feed controller
 *
 */
public class CQLFeedOnlineTest {

    /** the logger */
    private static final Logger logger = LoggerFactory.getLogger(CQLFeedOnlineTest.class.getName());

    @Test
    public void testFeedControllerCQL() throws Exception {
        Properties p = new Properties();
        p.put(FeedConfiguration.PROP_SUB_URI_NAME,"test");
        p.put(FeedConfiguration.PROP_NAME_ADAPTER_CLASS,"org.xbib.atom.ElasticsearchAbderaAdapter");
        p.put(FeedConfiguration.PROP_FEED_CONFIG_LOCATION_NAME, "");
        p.put(ElasticsearchAtomFeedFactory.FEED_URI_PROPERTY_KEY,"sniff://hostname:9300");
        p.put("feed.stylesheet","xsl/es-mods-atom.xsl");
        p.put("feed.author","Jörg Prante");
        p.put("feed.title.pattern","Ihre Suche war : {0}");
        p.put("feed.subtitle.pattern","{0} Treffer in {1} Sekunden");
        p.put("feed.constructiontime.pattern","Feed erzeugt in {0,number} Millisekunden");
        ElasticsearchAtomFeedFactory controller = new ElasticsearchAtomFeedFactory();
        try {
            Feed feed = controller.createFeed(p, "dc.title = test", 0, 10);
            StringWriter sw = new StringWriter();
            feed.writeTo("prettyxml", sw);
            logger.info(sw.toString());
        } catch (NoNodeAvailableException e) {
            logger.warn(e.getMessage());
        } catch (IOException e) {
            logger.warn(e.getMessage());
        }
    }
}
