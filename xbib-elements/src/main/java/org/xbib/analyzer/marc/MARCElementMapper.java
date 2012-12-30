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
package org.xbib.analyzer.marc;

import java.util.Map;
import org.xbib.elements.ElementBuilder;
import org.xbib.elements.ElementMapper;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;

public class MARCElementMapper 
    extends ElementMapper<FieldCollection, String, MARCElement, MARCContext> {
    
    /**
     * Instantiate a MARC element mapper.
     * @param format name of the configuration to be loaded
     */
    public MARCElementMapper(String format) {
        super("/org/xbib/analyzer/elements/", format);
    }
    
    /**
     * Process key/value stream of MARC data. The key/value stream is organized
     * with the subfield set as key and the field data as value.
     * THe builders of this mapper are iterated, and for each found mapped
     * element, the element field builders are invoked. Subfield processing
     * may be configured in the element settings. If there is a subfield
     * processing setting, call the element field builder for each subfield.
     * @param key the subfields 
     * @param value field data
     */
    @Override
    public void keyValue(FieldCollection key, String value) {
        if (key == null) {
            return;
        }
        MARCElement element = (MARCElement) getMap().get(key.getDesignators());
        for (ElementBuilder<FieldCollection, String, MARCElement, MARCContext> builder : getBuilders()) {            
            if (element != null) {
                // field builder - check for subfield type and pass configured values
                Map<String, String> subfields = (Map<String, String>) element.getSettings().get("subfields");
                if (subfields == null) {
                    element.fields(builder, key, value);
                } else {
                    for (Field field : key) {
                        String subfield = field.getSubfieldId();
                        element.field(builder, field, subfields.get(subfield));
                    }
                }
            } else {
                logger.debug("no element for '{}' fields={}", key.getDesignators(), key);
            }
            // call the builder with a global key/value pair, even if an element is not configured
            builder.build(element, key, value);
        }
    }    
    
}
