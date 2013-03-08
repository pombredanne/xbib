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
package org.xbib.analyzer.elements.marc;

import org.xbib.elements.ElementBuilder;
import org.xbib.elements.marc.MARCContext;
import org.xbib.elements.marc.MARCElement;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;

import java.util.Map;

public class PhysicalDescriptionCode extends MARCElement {
    private final static PhysicalDescriptionCode instance = new PhysicalDescriptionCode();
    
    public static MARCElement getInstance() {
        return instance;
    }
    
    @Override
    public void fields(ElementBuilder<FieldCollection, String, MARCElement, MARCContext> builder, FieldCollection fields, String value) {
        Map<String,Object> tags = (Map<String,Object>) getSettings().get("tags");
        if (tags == null) {
            return;
        }
        Map<String,Object> codes = (Map<String,Object>) getSettings().get("codes");
        if (codes == null) {
            return;
        }
        for (Field field: fields) {
            String data = field.data();
            for (String pos : codes.keySet()) {
                int i = Integer.parseInt(pos);
                Map<String,Object> m =  (Map<String,Object>)codes.get(data.substring(i,i+1));
                logger.info("phys: i={} data={} m={}", i, data.substring(i,i+1), m, field);
                if (m == null) {
                    continue;
                }
                String pred = (String)codes.get("value");
                if (pred == null) {
                    continue;
                }
                logger.info("got pred {}", pred);
                for (String pos2 : m.keySet()) {
                    int j = Integer.parseInt(pos2);
                    String code = (String)m.get(data.substring(j,j+1));
                    logger.info("got code {} for {}", code, data.substring(j,j+1));
                    if (code == null) {
                        continue;
                    }
                    builder.context().resource().add(pred, code);
                }
            }
        }
    }
    
}
