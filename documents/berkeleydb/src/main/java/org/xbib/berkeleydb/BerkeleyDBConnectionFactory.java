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
package org.xbib.berkeleydb;

import java.io.IOException;
import java.net.URI;
import org.xbib.io.Connection;
import org.xbib.io.ConnectionFactory;

/**
 * A Berkeley DB connection factory
 *
 */
public final class BerkeleyDBConnectionFactory implements ConnectionFactory<BerkeleyDBSession> {

    /**
     * A new connection
     *
     * @param uri the bdb URI
     *
     * @return a BDB Index connection
     *
     * @throws IOException if the BDB index can not be opened
     */
    @Override
    public Connection<BerkeleyDBSession> getConnection(final URI uri) throws IOException {
        BerkeleyDBConnection connection = new BerkeleyDBConnection();
        connection.setURI(uri);
        return connection;
    }

    /**
     * Check is a scheme is supported
     *
     * @param scheme the scheme to be checked
     *
     * @return true if supported
     */
    @Override
    public boolean providesScheme(String scheme) {
        return scheme.startsWith("bdb");
    }
}
