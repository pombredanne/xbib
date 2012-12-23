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
package org.xbib.io.iso23950;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import org.xbib.io.Session;

/**
 * Init / Search / Present operation with result processing
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public abstract class AbstractSearchRetrieve {

    private static final ResourceBundle recordSyntaxBundle =
            ResourceBundle.getBundle("org.xbib.io.iso23950.recordsyntax");
    private String user;
    private String password;
    private long timeout;
    private List<String> databases;
    private String query;
    private int offset;
    private int length;
    private String resultSetName;
    private String elementSetName;
    private String preferredRecordSyntax;
    private int resultCount;

    public AbstractSearchRetrieve setUser(String user) {
        this.user = user;
        return this;
    }

    public AbstractSearchRetrieve setPassword(String password) {
        this.password = password;
        return this;
    }

    public AbstractSearchRetrieve setTimeout(long seconds) {
        this.timeout = seconds;
        return this;
    }
    
    public long getTimeout() {
        return timeout;
    }

    public AbstractSearchRetrieve setDatabase(List<String> databases) {
        this.databases = databases;
        return this;
    }

    public AbstractSearchRetrieve setQuery(String query) {
        this.query = query;
        return this;
    }
    
    public String getQuery() {
        return query;
    }

    public AbstractSearchRetrieve setFrom(int offset) {
        this.offset = offset;
        return this;
    }

    public AbstractSearchRetrieve setSize(int length) {
        this.length = length;
        return this;
    }

    public AbstractSearchRetrieve setResultSetName(String resultSetName) {
        this.resultSetName = resultSetName;
        return this;
    }

    public AbstractSearchRetrieve setElementSetName(String elementSetName) {
        this.elementSetName = elementSetName;
        return this;
    }

    public AbstractSearchRetrieve setPreferredRecordSyntax(String preferredRecordSyntax) {
        this.preferredRecordSyntax = recordSyntaxBundle.containsKey(preferredRecordSyntax)
                ? recordSyntaxBundle.getString(preferredRecordSyntax.toLowerCase()) : preferredRecordSyntax;
        return this;
    }

    public void execute(ZSession session, RecordHandler handler) throws IOException {
        if (session == null) {
            throw new IOException("no session?");
        }
        if (query == null) {
            throw new IOException("no query");
        }
        if (timeout > 0) {
            session.getConnection().setTimeout(timeout);
        }
        if (offset < 1) {
            // Z39.50 bails out when offset = 0
            this.offset = 1;
        }
        this.resultCount = -1;
        long t0 = System.currentTimeMillis();
        if (!session.isOpen()) {
            session.open(Session.Mode.READ);
            if (!session.isOpen()) {
                session.getSessionLogger().error("{} unable to open session [{}]", databases, query);
                throw new IOException("session not open");
            }
            if (!session.authenticated()) {
                InitOperation init = new InitOperation(user, password, null);
                init.execute(session);
                session.setAuthenticated(!init.rejected());
                if (!session.authenticated()) {
                    session.getSessionLogger().error("{} session not authenticated [{}]", databases, query);
                    throw new IOException("could not authenticate");
                }
            }
        }
        AbstractSearchOperation search = getSearchOperation(databases, resultSetName);
        search.setTimeout(getTimeout() * 1000);
        search.query(session, query);
        long presentMillis = 0L;
        if (!search.isSuccess()) {
            session.getSessionLogger().warn("{} search was not a success [{}]", databases, query);
        } else {
            PresentOperation present = new PresentOperation(
                    resultSetName, elementSetName, preferredRecordSyntax,
                    offset, length);
            present.execute(session, handler);
            presentMillis = present.getMillis();
        }
        long t1 = System.currentTimeMillis();
        this.resultCount = search.getResultCount();
        session.getSessionLogger().info("{} [{}ms] [{}ms] [{}ms] [{}] [{}]",
                databases, t1 - t0, search.getMillis(), presentMillis, resultCount, query);
        if (!search.isSuccess()) {
            throw new IOException("search was not a success");
        }
    }

    public int getResultCount() {
        return resultCount;
    }

    public abstract AbstractSearchOperation getSearchOperation(List<String> database, String resultSetName);
}
