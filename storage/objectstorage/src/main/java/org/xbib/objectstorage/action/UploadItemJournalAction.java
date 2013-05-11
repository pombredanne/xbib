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

import org.xbib.objectstorage.Container;
import org.xbib.objectstorage.ItemInfo;
import org.xbib.objectstorage.ObjectStorageRequest;
import org.xbib.util.ILL;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UploadItemJournalAction extends AbstractInsertAction {

    private final ItemInfo itemInfo;
    private final Container container;

    public UploadItemJournalAction(String sql, ItemInfo itemInfo, Container container) {
        super(sql);
        this.itemInfo = itemInfo;
        this.container = container;
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
        final Map<String, Object> params = new HashMap<>();
        params.put(ITEM_PARAMETER, id);
        params.put(NAME_PARAMETER, request.getUserAttributes().getName());
        params.put("message", itemInfo.isWrittenSuccessfully() ?
                "Datei " + itemInfo.getKey().getName()
                        + ", Ordner '" + container.getDescription()
                        + "', Client " + request.getUser()
                        + ", " + itemInfo.getOctets() + " bytes, SHA1 " + itemInfo.getChecksum()
                : "Ein Fehler ist aufgetreten beim Schreiben der Datei " + itemInfo.getKey().getName()
                + " in den Ordner " + container.getDescription()
        );
        params.put("msgcode", itemInfo.isWrittenSuccessfully() ? "UPLOAD" : "IOERROR");
        logger.debug("upload item journal action = {} params = {}", sql, params);
        return params;
    }
}
