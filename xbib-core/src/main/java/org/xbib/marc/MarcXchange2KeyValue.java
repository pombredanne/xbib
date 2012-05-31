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
package org.xbib.marc;

import org.xbib.keyvalue.KeyValueStreamListener;

public class MarcXchange2KeyValue implements
        MarcXchangeListener,
        KeyValueStreamListener<FieldDesignatorList, String> {

    private FieldDesignatorList fields = new FieldDesignatorList();
    private KeyValueStreamListener<FieldDesignatorList, String> listener;

    public MarcXchange2KeyValue setListener(KeyValueStreamListener<FieldDesignatorList, String> listener) {
        this.listener = listener;
        return this;
    }

    public KeyValueStreamListener<FieldDesignatorList, String> getKeyValueListener() {
        return listener;
    }

    @Override
    public void begin() {
        if (listener != null) {
            listener.begin();
        }
    }

    @Override
    public void end() {
        if (listener != null) {
            listener.end();
        }
    }

    @Override
    public void end(Object trailer) {
        if (listener != null) {
            listener.end(trailer);
        }
    }
    @Override
    public void keyValue(FieldDesignatorList key, String value) {
        if (listener != null && key != null && value != null) {
            listener.keyValue(key, value);
        }
    }

    @Override
    public void beginRecord(String format, String type) {
        begin();
        keyValue(FieldDesignatorList.FORMAT_KEY, format);
        keyValue(FieldDesignatorList.TYPE_KEY, type);
    }

    @Override
    public void endRecord() {
        end();
    }

    @Override
    public void leader(String label) {
        keyValue(FieldDesignatorList.LEADER_KEY, label);
    }
    
    @Override
    public void trailer(String trailer) {
        end(trailer);
    }

    @Override
    public void beginControlField(FieldDesignator designator) {
        fields.add(designator);
    }

    @Override
    public void endControlField(FieldDesignator designator) {
        keyValue(fields, designator != null ? designator.getData() : null);
        fields.clear();
    }

    @Override
    public void beginDataField(FieldDesignator designator) {
        fields.add(designator);
    }

    @Override
    public void endDataField(FieldDesignator designator) {
        String data = designator != null ? designator.getData() : null;
        if (data == null && fields.size() == 1) {
            data = fields.get(0).getData();
        }
        keyValue(fields, data);
        fields.clear();
    }

    @Override
    public void beginSubField(FieldDesignator designator) {
        // do nothing
    }

    @Override
    public void endSubField(FieldDesignator designator) {
        if (designator != null) {
            // remove last designator if there is no sub field (data field)
            int n = fields.size();
            if (n > 0 && !fields.get(n - 1).isSubField()) {
                fields.remove(n - 1);
            }
            fields.add(designator);
        }
    }
}
