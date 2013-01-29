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
package org.xbib.marc.addons;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import org.xbib.io.Connection;
import org.xbib.io.ConnectionService;
import org.xbib.io.Session;
import org.xbib.io.jdbc.operator.Query;
import org.xbib.io.jdbc.NotclosedSQLResultSetListener;
import org.xbib.io.jdbc.SQLSession;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

/**
 * Aleph publishing incremental updates
 *
 */
public class AlephPublishingIterator implements Closeable, Iterator<Integer> {

    private final static Logger logger = LoggerFactory.getLogger(AlephPublishingIterator.class.getName());
    private Connection<SQLSession> connection;
    private SQLSession session;
    private ResultSet results;
    private URI uri;
    private String library;
    private String name;
    private String fromTime;
    private String toTime;
    private boolean error;
    private String id;
    private int count;

    public AlephPublishingIterator(String library) {
        this.library = library;
    }

    public AlephPublishingIterator setURI(URI uri) {
        this.uri = uri;
        return this;
    }

    public AlephPublishingIterator setLibrary(String library) {
        this.library = library;
        return this;
    }

    public AlephPublishingIterator setName(String name) {
        this.name = name;
        return this;
    }

    public AlephPublishingIterator setFrom(String from) {
        this.fromTime = from + "0000000";
        return this;
    }

    public AlephPublishingIterator setTo(String to) {
        this.toTime = to + "0000000";
        return this;
    }

    @Override
    public boolean hasNext() {
        if (session == null) {
            createSession();
        }
        if (results == null) {
            return false;
        }
        try {
            if (results.next()) {
                this.id = results.getString(1);
                return true;
            } else {
                close();
            }
        } catch (SQLException | IOException e) {
            logger.error(e.getMessage(), e);
            try {
                close();
                error = true;
            } catch (Exception ex) {
                logger.error(ex.getMessage());
            }
        }
        return false;
    }

    @Override
    public Integer next() {
        if (results == null) {
            return null;
        }
        if (id != null) {
            count++;
            return Integer.valueOf(id);
        } else {
            return null;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public void close() throws IOException {
        try {
            if (results != null) {
                results.close();
                results = null;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (session != null) {
                session.close();
                session = null;
            }
            if (connection != null) {
                connection.close();
                connection = null;
            }
        }
        logger.info("update iterator closed after {} elements {}", count, error ? "with error" : "");
    }

    private void createSession() {
        this.error = false;
        this.count = 0;
        NotclosedSQLResultSetListener p = new NotclosedSQLResultSetListener();
        try {
            this.connection = ConnectionService.getInstance()
                    .getConnectionFactory("jdbc")
                    .getConnection(uri);
            this.session = connection.createSession();
            session.open(Session.Mode.READ);
            Query query = new Query("select /*+ index(z00p z00p_id5) */ z00p_doc_number from " + library
                    + ".z00p where z00p_set = '" + name + "' and z00p_timestamp between '" + fromTime + "' and '" + toTime + "'");
            query.addListener(p);
            query.execute(session);
            this.results = p.getResultSet();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
