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
package org.xbib.objectstorage.adapter;

import org.xbib.io.jdbc.NotclosedSQLResultSetListener;
import org.xbib.io.jdbc.SQLSession;
import org.xbib.io.jdbc.operator.Query;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JDBCUserAttributes implements UserAttributes {

    private final static Logger logger = Logger.getLogger(JDBCUserAttributes.class.getName());
    private final ResourceBundle bundle = ResourceBundle.getBundle("org.xbib.filestorage.service.userattributes");
    private final Map<String, Object> params = new HashMap<>();
    private final SQLSession session;
    private final String user;
    private Map<String, String> attributes;

    public JDBCUserAttributes(SQLSession session, String user) throws SQLException, IOException {
        this.session = session;
        this.user = user;
        this.attributes = new HashMap();
        retrieve();
    }

    private void retrieve() throws SQLException, IOException {
        params.put("user", user);
        final Query query = new Query(bundle.getString("getuserattributes"),
                new String[]{"user"}, params);
        String[] keys = bundle.getString("userattributes").split(",");
        NotclosedSQLResultSetListener p = new NotclosedSQLResultSetListener();
        try {
            query.addListener(p);
            query.execute(session);
            ResultSet result = p.getResultSet();
            SQLWarning warning = result.getWarnings();
            while (warning != null) {
                logger.log(Level.WARNING, "{0} {1}", new Object[]{warning.getMessage(), warning.getSQLState()});
                warning = warning.getNextWarning();
            }
            if (result != null && result.next()) {
                for (int i = 0; i < result.getMetaData().getColumnCount(); i++) {
                    attributes.put(keys[i], result.getString(i + 1));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage() + query.getSQL(), e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage() + query.getSQL(), e);
        } finally {
            p.close();
        }
    }

    @Override
    public String getName() {
        return attributes.get("name");
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

}
