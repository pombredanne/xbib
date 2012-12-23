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
package org.xbib.analyzer.marc.extensions.pica;

import java.util.Map;
import org.xbib.elements.ElementBuilder;
import org.xbib.elements.ElementMapper;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;

public class PicaElementMapper
        extends ElementMapper<FieldCollection, String, PicaElement, PicaContext> {

    public PicaElementMapper(String format) {
        super("/org/xbib/analyzer/elements/", format);
    }

    @Override
    public void keyValue(FieldCollection key, String value) {
        if (key == null) {
            return;
        }
        PicaElement e = (PicaElement) getMap().get(key.getDesignators());
        for (ElementBuilder<FieldCollection, String, PicaElement, PicaContext> builder : getBuilders()) {
            if (e != null) {
                // field builder - check for subfield type and pass configured values
                Map<String, String> subfields = (Map<String, String>) e.getSettings().get("subfields");
                if (subfields == null) {
                    e.fields(builder, key, value);
                } else {
                    for (Field field : key) {
                        String subfield = field.getSubfieldId();
                        e.field(builder, field, subfields.get(subfield));
                    }
                }
            } else {
                logger.info("no element for {}", key.getDesignators());
            }
            // call the builder with a global key/value pair, even when e is null
            builder.build(e, key, value);
        }
    }
}
