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
package org.xbib.objectstorage.action;

import org.xbib.objectstorage.Action;
import org.xbib.objectstorage.ObjectStorageRequest;
import org.xbib.objectstorage.ObjectStorageResponse;
import org.xbib.objectstorage.action.sql.SQLService;
import org.xbib.objectstorage.adapter.AbstractAdapter;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public abstract class AbstractInsertAction extends AbstractQueryAction {

    public AbstractInsertAction(String sql) {
        super(sql);
    }

    @Override
    public Action waitFor(long l, TimeUnit tu) throws IOException {
        return this;
    }

    @Override
    public void execute(ObjectStorageRequest request, ObjectStorageResponse response) throws Exception {
        if (request.getAdapter() instanceof AbstractAdapter) {
            long t0 = System.currentTimeMillis();
            AbstractAdapter a = (AbstractAdapter) request.getAdapter();
            SQLService service = SQLService.getInstance(a);
            try (PreparedStatement p = service.getConnection().prepareStatement(sql)) {
                boolean success = service.execute(service.bind(p, createBindKeys(), createParams(request)));
                response.builder().status(success ? 200 : 500);
            } catch (SQLException | IOException e) {
                logger.error(e.getMessage(), e);
                throw e;
            } finally {
                long t1 = System.currentTimeMillis();
                response.builder().header("X-insert-millis", t1 - t0);
                logger.debug("insert took {} ms", t1 - t0);
            }
        }
    }

    @Override
    protected int buildResponse(ResultSet result, ObjectStorageRequest request, ObjectStorageResponse response) throws SQLException {
        return -1;
    }

}
