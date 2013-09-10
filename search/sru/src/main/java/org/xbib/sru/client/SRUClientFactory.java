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
package org.xbib.sru.client;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.WeakHashMap;

/**
 *  A factory for SRU clients
 */
public final class SRUClientFactory {

    private final static SRUClientFactory instance = new SRUClientFactory();

    private final static Logger logger = LoggerFactory.getLogger(SRUClientFactory.class.getName());

    private final static Map<URI, SRUClient> clients = new WeakHashMap();

    private SRUClientFactory() {
        ServiceLoader<SRUClient> loader = ServiceLoader.load(SRUClient.class);
        Iterator<SRUClient> iterator = loader.iterator();
        while (iterator.hasNext()) {
            SRUClient client = iterator.next();
            if (!clients.containsKey(client.getClientIdentifier())) {
                clients.put(client.getClientIdentifier(), client);
            }
        }
    }

    public static SRUClientFactory getInstance() {
        return instance;
    }

    public static SRUClient getDefaultClient() {
        return clients.isEmpty() ? null : clients.entrySet().iterator().next().getValue();
    }

    public static SRUClient newClient(URI uri) {
        if (clients.containsKey(uri)) {
            return clients.get(uri);
        } else {
            try {
                SRUClient client = new DefaultSRUClient(uri);
                clients.put(uri, client);
                return client;
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
                return null;
            }
        }
    }

    public static SRUClient newClient(String name) {
        try {
            Properties properties = new Properties();
            InputStream in = instance.getClass().getResourceAsStream("/org/xbib/sru/client/" + name + ".properties");
            if (in != null) {
                properties.load(in);
            }
            if (in == null || properties.isEmpty()) {
                throw new IllegalArgumentException("SRU client " + name + " properties not found");
            }
            PropertiesSRUClient client = new PropertiesSRUClient(properties);
            clients.put(client.getClientIdentifier(), client);
            return client;
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            return null;
        }
    }

    public static void shutdown() {
        for (SRUClient client : clients.values()) {
            try {
                client.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

}
