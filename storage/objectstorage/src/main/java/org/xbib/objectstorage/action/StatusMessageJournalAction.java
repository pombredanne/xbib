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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.xbib.objectstorage.ItemInfo;
import org.xbib.objectstorage.ObjectStorageRequest;
import org.xbib.util.ILL;

public class StatusMessageJournalAction extends AbstractInsertAction {

    private final ItemInfo itemInfo;

    public StatusMessageJournalAction(String sql, ItemInfo itemInfo) {
        super(sql);
        this.itemInfo = itemInfo;
    }

    @Override
    protected String[] createBindKeys() {
        return new String[]{ITEM_PARAMETER, "message", NAME_PARAMETER, "msgcode"};
    }

    @Override
    protected Map<String, Object> createParams(ObjectStorageRequest request) throws IOException {
        ILL ill = new ILL(request.getItem());
        if (!ill.isValid()) {
            throw new IllegalArgumentException("invalid item");
        }
        long id = ill.getNumber();
        InputStream in = itemInfo.getInputStream();
        StringBuilder sb = new StringBuilder();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            sb.append(new String(b, 0, n));
        }
        final Map<String, Object> params = new HashMap<>();
        params.put("message", sb.toString());
        params.put("id", id);
        params.put(NAME_PARAMETER, request.getUserAttributes().getName());
        params.put("msgcode", itemInfo.getMessage().getCode());
        return params;
    }
    
}
