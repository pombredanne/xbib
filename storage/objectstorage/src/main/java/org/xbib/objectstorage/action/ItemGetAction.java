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

import org.xbib.objectstorage.ObjectStorageRequest;
import org.xbib.objectstorage.ObjectStorageResponse;
import org.xbib.objectstorage.adapter.container.rows.ItemRow;
import org.xbib.util.ILL;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemGetAction extends AbstractQueryAction {

    public ItemGetAction(String sql) {
        super(sql);
    }

    @Override
    protected String[] createBindKeys() {
        return new String[]{ITEM_PARAMETER, ITEM_PARAMETER, NAME_PARAMETER};
    }

    @Override
    protected Map<String, Object> createParams(ObjectStorageRequest request) throws IOException {
        final Map<String, Object> params = new HashMap<>();
        ILL ill = new ILL(request.getItem());
        if (!ill.isValid()) {
            throw new IllegalArgumentException("invalid item");
        }
        long id = ill.getNumber();
        params.put(ITEM_PARAMETER, id);
        params.put(NAME_PARAMETER, request.getUserAttributes().getName());
        logger.debug("item get action = {} params={} id={}", sql, params, id);
        return params;
    }

    @Override
    protected int buildResponse(ResultSet result, ObjectStorageRequest request, ObjectStorageResponse response)
            throws SQLException {
        List<ItemRow> rows = new ArrayList<>();
        while (result.next()) {
            final ItemRow row = new ItemRow();
            row.setDateRequested(result.getString(1));
            row.setDateOfLastTransition(result.getString(2));
            row.setMostRecentService(result.getString(3));
            row.setDateOfMostRecentService(result.getString(4));
            row.setMostRecentServiceNote(result.getString(5));
            row.setServiceType(result.getString(6));
            row.setState(result.getString(7));
            row.setRequestType(result.getString(8));
            row.setSupplier(result.getString(9));
            row.setSupplierName(result.getString(10));
            row.setItemTitle(result.getString(11));
            row.setItemReferenceSource(result.getString(12));
            row.setItemReferenceCode(result.getString(13));
            row.setRequester(result.getString(14));
            row.setRequesterName(result.getString(15));
            rows.add(row);
        }
        response.setItemResponse(rows);
        return rows.size();
    }
}
