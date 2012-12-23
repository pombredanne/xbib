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
import java.util.ArrayList;
import org.xbib.io.Connection;
import org.xbib.io.Session;
import org.xbib.iri.IRI;

/**
 * A Berkeley DB connection
 *
 */
public class BerkeleyDBConnection implements Connection<BerkeleyDBSession> {

    private ArrayList<BerkeleyDBSession> sessions = new ArrayList<BerkeleyDBSession>();
    private URI uri;

    public BerkeleyDBConnection() {
    }

    @Override
    public BerkeleyDBConnection setURI(URI uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    /**
     * Create a Berkeley DB session
     * @return the BDB session
     * @throws IOException
     */
    @Override
    public BerkeleyDBSession createSession() throws IOException {
        BerkeleyDBSession session = new BerkeleyDBSession(IRI.create(uri.toString()));
        sessions.add(session);
        return session;
    }

    @Override
    public void close() throws IOException {
        for (Session session : sessions) {
            session.close();
        }
    }
}
