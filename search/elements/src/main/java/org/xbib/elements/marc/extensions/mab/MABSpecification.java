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
package org.xbib.elements.marc.extensions.mab;

import org.xbib.elements.Element;
import org.xbib.elements.Specification;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Element Map
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class MABSpecification extends Specification {

    private final static Logger logger = LoggerFactory.getLogger(MABSpecification.class.getName());

    protected Map addSpec(String value, Element element, Map map) {
        int pos = value.indexOf('$');
        String h = pos > 0 ? value.substring(0,pos) : null;
        String t = pos > 0 ? value.substring(pos+1) : value;
        addSegment(h, t, element, map);
        return map;
    }

    private Map addSegment(String head, String tail, Element element, Map map) {
        if (head == null) {
            if (map.containsKey(tail)) {
                logger.warn("already exist in map: {} {}", tail, map);
                return map;
            }
            map.put(tail, element);
            return map;
        }
        int pos = tail != null ? tail.indexOf('$') : 0;
        String h = pos > 0 ? tail.substring(0,pos) : null;
        String t = pos > 0 ? tail.substring(pos+1) : tail;
        Object o = map.get(head);
        if (o != null) {
            addSegment(h, t, element, (Map)o);
            return map;
        } else {
            Map m = new HashMap();
            Map n = addSegment(h, t, element, m);
            map.put(head, n);
            return map;
        }
    }

}
