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
package org.xbib.elements;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.xbib.keyvalue.KeyValueStreamListener;

public class ElementMapper<E extends Element, C extends ElementContext, K, V>
        implements KeyValueStreamListener<K, V> {

    protected final static Logger logger = Logger.getLogger(ElementMapper.class.getName());
    private final Map<String, Element> map;
    private List<ElementBuilder<K, V, E, C>> builders = new ArrayList();

    public ElementMapper(String format) {
        try {
            this.map = ElementMapFactory.getElementMap(format);
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public ElementMapper addBuilder(ElementBuilder<K, V, E, C> builder) {
        builders.add(builder);
        return this;
    }

    
    @Override
    public void begin() {
        for (ElementBuilder<K, V, E, C> builder : builders) {
            builder.begin();
        }
    }

    @Override
    public void keyValue(K key, V value) {
        if (key == null) {
            return;
        }        
        E e = (E) map.get(key.toString());
        for (ElementBuilder<K, V, E, C> builder : builders) {
            if (e != null) {
                e.build(builder, key, value);
            }
            builder.build(e, key, value);
        }
    }

    @Override
    public void end() {
        for (ElementBuilder<K, V, E, C> builder : builders) {
            builder.end();
        }
    }

    @Override
    public void end(Object metadata) {
        for (ElementBuilder<K, V, E, C> builder : builders) {
            builder.end(metadata);
        }
    }

}
