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
 */package org.xbib.io;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

/**
 * A simple onnection pool for sharing connections between threads
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public final class ConnectionPool {

    /** the pool */
    private static final HashMap<URI, Connection<? extends Session>> pool = 
    	new HashMap<>();

    private ConnectionPool() {
    }

    /**
     * Close pool
     */
    public static synchronized void close() throws IOException {
        for (Connection<? extends Session> c : pool.values()) {
            c.close();
        }
        pool.clear();
    }

    /**
     * Get connection from pool or create new one if it does not exist
     *
     * @param uri the key
     *
     * @return the connection
     *
     * @throws IOException if new connection can not be established
     */
    public static synchronized Connection<? extends Session> getConnection(URI uri) throws IOException {
        Connection<? extends Session> connection = pool.get(uri);

        if (connection != null) {
            return connection;
        } else {
            pool.put(uri, ConnectionManager.getConnection(uri));

            return pool.get(uri);
        }
    }
    
    /**
     * Release connection back to pool
     * @param connection the connection
     * 
     */
    public static synchronized void releaseConnection(Connection<?> connection) throws IOException {
        if (connection == null)
            throw new IllegalArgumentException("connection must not be null");
        try {
            connection.close();
        } finally {
            pool.remove(connection.getURI());
        }
    }
}
