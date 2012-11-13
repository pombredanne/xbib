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
package org.xbib.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * The Connection manager object
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public final class ConnectionManager {

    private ConnectionManager() {
    }

    /**
     * Gets a new un-pooled, un-threaded connection by name
     *
     * @return a connection
     *
     * @throws IOException
     */
    public synchronized static Connection<? extends Session> getConnection(String name)
            throws IOException {
        return getConnection(name, false);
    }

    /**
     * Gets a new un-pooled, un-threaded connection
     *
     * @param uri the connection URI
     *
     * @return a connection
     *
     * @throws IOException
     */
    public synchronized static Connection<? extends Session> getConnection(URI uri)
            throws IOException {
        return getConnection(uri, false, false);
    }

    /**
     * Get connection
     *
     * @param name
     * @param pooled
     * @return a Connection
     * @throws IOException
     */
    public synchronized static Connection<? extends Session> getConnection(String name, boolean pooled)
            throws IOException {
        return getConnection(URI.create(name), pooled, false);
    }

    /**
     * Get a connection
     *
     * @param name
     * @param pooled
     * @param threaded
     * @return a Connection
     * @throws IOException
     */
    public synchronized static Connection<? extends Session> getConnection(String name, boolean pooled, boolean threaded)
            throws IOException {
        return getConnection(URI.create(name), pooled, threaded);
    }

    /**
     * Get a connection. <li> via jndi </li> if that fails, the following will
     * be tried: <li> via ServiceLoader. The ServiceLoader loads all classes
     * which are defined in WEB-INF/services/org.xbib.io.ConnectionFactory. All
     * theses classes should be an instance of the ConnectionFactory-interface
     * which has a method 'providesScheme'. If a ConnectionFactory equals the
     * scheme which is given through the parameter 'URI' the connection of this
     * ConnectionFactory is returned. </li>
     *
     * @see ServiceLoader
     * @param uri the URI
     * @param pooled if the connection is pooled
     * @param threaded if the connection URI is named by thread
     * @return a connection
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public synchronized static Connection<? extends Session> getConnection(URI uri, boolean pooled, boolean threaded)
            throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException("no connection URI given");
        }
        String jndiURL = "java:comp/env/" + uri.toString();
        ConnectionFactory<? extends Session> factory = null;
        Context context;
        boolean found = false;
        try {
            context = new InitialContext();
            Object o = context.lookup(jndiURL);
            if (o instanceof ConnectionFactory) {
                factory = (ConnectionFactory<? extends Session>) o;
                found = uri.getScheme() != null && factory.providesScheme(uri.getScheme());
            }
        } catch (NamingException e) {
            ServiceLoader<ConnectionFactory> loader = ServiceLoader.load(ConnectionFactory.class);
            Iterator<ConnectionFactory> it = loader.iterator();
            while (it.hasNext()) {
                factory = it.next();
                if (uri.getScheme() != null && factory.providesScheme(uri.getScheme())) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            throw new ServiceConfigurationError("no connection factory found for URI scheme " + uri.getScheme());
        }
        if (threaded) {
            try {
                uri = new URI(uri.getScheme(), uri.getAuthority(),
                        uri.getPath() + "/" + Thread.currentThread().getName(),
                        uri.getQuery(), uri.getFragment());
            } catch (URISyntaxException e) {
                throw new ServiceConfigurationError("invalid URI '" + uri + "', reason: " + e.getMessage());
            }
        }
        return pooled ? ConnectionPool.getConnection(uri) : factory.getConnection(uri);
    }

    public synchronized static void releaseConnection(Connection<? extends Session> connection) throws IOException {
        ConnectionPool.releaseConnection(connection);
    }

    /**
     * Ping if a host/port is reachable. Open TCP/IP socket on host/port with
     * timeout
     *
     * @param host the host
     * @param port the port
     * @param timeout timeout in milliseconds
     * @throws IOException
     */
    public synchronized static void ping(String host, int port, int timeout)
            throws IOException {
        if (host != null && port > 0) {
            try (Socket socket = new Socket()) {
                socket.setSoTimeout(timeout);
                socket.connect(new InetSocketAddress(host, port), timeout);
            }
        }
    }
}
