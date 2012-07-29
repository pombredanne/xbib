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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.xbib.io.util.DateUtil;
import org.xbib.objectstorage.ObjectStorageRequest;
import org.xbib.objectstorage.ObjectStorageResponse;
import org.xbib.objectstorage.adapter.container.rows.ContainerRow;
import org.xbib.standardnumber.InvalidStandardNumberException;
import org.xbib.util.ILL;

public class ContainerGetAction extends AbstractQueryAction {

    public ContainerGetAction(String sql) {
        super(sql);
    }

    @Override
    public void execute(ObjectStorageRequest request, ObjectStorageResponse response) throws Exception {
        super.execute(request, response);
    }
    
    @Override
    protected String[] createBindKeys() {
        return new String[]{NAME_PARAMETER, STATE_PARAMETER, SIZE_PARAMETER, FROM_PARAMETER};
    }

    @Override
    protected Map<String,Object> createParams(ObjectStorageRequest request) throws IOException {
        final Map<String, Object> params = new HashMap<>();
        params.put(NAME_PARAMETER, request.getUserAttributes().getName());
        params.put(STATE_PARAMETER, request.getStringParameter(STATE_PARAMETER, "PENDING"));
        params.put(SIZE_PARAMETER, request.getLongParameter(FROM_PARAMETER, 1L) + request.getLongParameter(SIZE_PARAMETER, 10L) - 1);
        params.put(FROM_PARAMETER, request.getLongParameter(FROM_PARAMETER, 1L));
        return params;
    }

    @Override
    protected int buildResponse(ResultSet result, ObjectStorageRequest request, ObjectStorageResponse response) throws SQLException {
        List<ContainerRow> rows = new ArrayList<>();
        while (result.next()) {
            ContainerRow row = new ContainerRow();
            row.setPosition(result.getString(1));
            String d = result.getString(2);
            Date date = DateUtil.parseDateISO(d);
            row.setDateRequested(d);
            row.setDateOfLastTransition(result.getString(3));
            int year = DateUtil.getYear(date);
            String transaction = result.getString(4);
            String authority = request.getStringParameter(AUTHORITY_PARAMETER, "XX-000");
            String id = authority + "-" + year + "-" + transaction + "-0";
            try {
                ILL ill = new ILL(id); // checksum added automatically
                String item = ill.getStandardNumberPrintableRepresentation();
                row.setItem(item);
            } catch (InvalidStandardNumberException ex) {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
            rows.add(row);
        }
        response.setContainerResponse(rows);
        return rows.size();
    }
}
