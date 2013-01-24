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

import java.util.ArrayList;
import java.util.List;
import org.xbib.keyvalue.KeyValueStreamListener;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

/**
 * Convert a MarcXchange stream to a key/value stream. With optional value
 * string transformation.
 *
 * @author Jörg Prante <joergprante@gmail.com>
 */
public class MarcXchange2KeyValue implements
        MarcXchangeListener,
        KeyValueStreamListener<FieldCollection, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(MarcXchange2KeyValue.class.getName());

    public interface FieldDataTransformer {

        String transform(String value);
    }
    private FieldCollection fields = new FieldCollection();
    private List<KeyValueStreamListener<FieldCollection, String>> listeners = new ArrayList();
    private FieldDataTransformer transformer;

    public MarcXchange2KeyValue addListener(KeyValueStreamListener<FieldCollection, String> listener) {
        this.listeners.add(listener);
        return this;
    }

    public MarcXchange2KeyValue transformer(FieldDataTransformer transformer) {
        this.transformer = transformer;
        return this;
    }

    @Override
    public void begin() {
        for (KeyValueStreamListener<FieldCollection, String> listener : listeners) {
            listener.begin();
        }
    }

    @Override
    public void end() {
        for (KeyValueStreamListener<FieldCollection, String> listener : listeners) {
            listener.end();
        }
    }

    @Override
    public void end(Object trailer) {
        for (KeyValueStreamListener<FieldCollection, String> listener : listeners) {
            listener.end(trailer);
        }
    }

    @Override
    public void keyValue(FieldCollection key, String value) {
        for (KeyValueStreamListener<FieldCollection, String> listener : listeners) {
            // we allow null value here to be passed to the listeners
            if (key != null) {
                listener.keyValue(key, value);
            }
        }
    }

    @Override
    public void beginRecord(String format, String type) {
        begin();
        keyValue(FieldCollection.FORMAT_KEY, format);
        keyValue(FieldCollection.TYPE_KEY, type);
    }

    @Override
    public void endRecord() {
        end();
    }

    @Override
    public void leader(String label) {
        keyValue(FieldCollection.LEADER_KEY, label);
    }

    @Override
    public void trailer(String trailer) {
        end(trailer);
    }

    @Override
    public void beginControlField(Field field) {
        fields.add(field);
    }

    @Override
    public void endControlField(Field field) {
        String data = field != null ? field.data() : null;
        // transform field data?
        if (transformer != null && data != null) {
            data = transformer.transform(data);
        }
        keyValue(fields, data);
        fields.clear();
    }

    @Override
    public void beginDataField(Field field) {
        fields.add(field);
    }

    @Override
    public void endDataField(Field field) {
        // put data into the emitter if the only have one field
        String data = field != null ? field.data() : null;
        if (data == null && fields.size() == 1) {
            data = fields.getFirst().data();
        }
        // transform field data?
        if (transformer != null && data != null) {
            data = transformer.transform(data);
        }
        // emit fields as key/value
        keyValue(fields, data);
        fields.clear();
    }

    @Override
    public void beginSubField(Field field) {
        // do nothing
    }

    @Override
    public void endSubField(Field field) {
        if (field != null) {
            // remove last field if there is no sub field (it must be a data field)
            if (!fields.isEmpty() && !fields.getLast().isSubField()) {
                Field f = fields.removeLast();
            }            
            // transform field data?
            if (transformer != null && field.data() != null) {
                field.data(transformer.transform(field.data()));
            }
            fields.add(field);
        }
    }
}
