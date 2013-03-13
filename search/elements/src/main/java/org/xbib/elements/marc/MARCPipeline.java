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
package org.xbib.elements.marc;

import org.xbib.elements.ElementBuilderFactory;
import org.xbib.elements.ElementMap;
import org.xbib.elements.KeyValuePipeline;
import org.xbib.keyvalue.KeyValue;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.rdf.Resource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class MARCPipeline
        extends KeyValuePipeline<FieldCollection, String, MARCElement, MARCContext> {

    private final Logger logger = LoggerFactory.getLogger(MARCPipeline.class.getName());

    public MARCPipeline(int i,
                        BlockingQueue<List<KeyValue>> queue,
                        Map map,
                        ElementBuilderFactory<FieldCollection, String, MARCElement, MARCContext> factory) {
        super(i, queue, map, factory);
    }

    /**
     * Pipeline processing of the fields/value stream of MARC data. The fields/value stream is
     * organized with the subfield set as fields and the field data as value.
     * THe builders of this mapper are iterated, and for each found mapped
     * element, the element field builders are invoked. Subfield processing may
     * be configured in the element settings. If there is a subfield processing
     * setting, call the element field builder for each subfield.
     */
    @Override
    protected void build(FieldCollection fields, String value) {
        if (fields == null) {
            return;
        }
        String key = fields.toString();
        MARCElement element = (MARCElement) ElementMap.getElement(key, map());
        if (element != null) {
            // field builder - check for subfield type and pass configured values
            Map<String, Object> subfields = (Map<String, Object>) element.getSettings().get("subfields");
            if (subfields == null) {
                element.fields(builder(), fields, value);
            } else {
                Resource resource = null;
                if (subfields.containsKey("_predicate")) {
                    resource = builder().context().resource().newResource((String) subfields.get("_predicate"));
                } else {
                    Map<String, Object> indicators = (Map<String, Object>) element.getSettings().get("indicators");
                    if (indicators != null) {
                        String ind = (String) indicators.get(fields.getFirst().indicator());
                        resource = builder().context().resource().newResource(ind != null ? ind : "defaultresource");
                    }
                }
                if (resource != null) {
                    for (Field field : fields) {
                        Map.Entry<String, Object> me = SubfieldValueMapper.map(subfields, field);
                        if (me.getKey() != null) {
                            resource.add(me.getKey(), me.getValue().toString());
                        }
                    }
                } else {
                    element.fields(builder(), fields, value);
                }
            }
        } else {
            if (detectUnknownKeys) {
                unknownKeys.add(key);
                if (logger.isDebugEnabled()) {
                    logger.debug("unknown key detected: {} {}", fields, value);
                }
            }
        }
        // call the builder with a global fields/value pair, even if an element is not configured
        builder().build(element, fields, value);
    }

}
