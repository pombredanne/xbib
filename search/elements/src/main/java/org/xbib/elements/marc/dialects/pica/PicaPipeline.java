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
package org.xbib.elements.marc.dialects.pica;

import org.xbib.elements.AbstractSpecification;
import org.xbib.elements.marc.SubfieldValueMapper;
import org.xbib.elements.ElementBuilderFactory;
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

public class PicaPipeline extends KeyValuePipeline<FieldCollection, String, PicaElement, PicaContext> {

    private final Logger logger = LoggerFactory.getLogger(PicaPipeline.class.getName());

    public PicaPipeline(int i,
                        AbstractSpecification specification,
                        BlockingQueue<List<KeyValue>> queue,
                        Map map,
                        ElementBuilderFactory<FieldCollection, String, PicaElement, PicaContext> factory) {
        super(i, specification, queue, map, factory);
    }

    @Override
    protected void build(FieldCollection fields, String value) {
        if (fields == null) {
            return;
        }
        String key = fields.toSpec();
        PicaElement element = (PicaElement) specification.getElement(key, map());
        if (element != null) {
            // element-based processing
            element.fields(builder(), fields, value);
            // optional indicator configuration
            Map<String, Object> indicators = (Map<String, Object>) element.getSettings().get("indicators");
            // optional subfield configuration
            Map<String, Object> subfields = (Map<String, Object>) element.getSettings().get("subfields");
            if (subfields != null) {
                // get current resource and create new anoymous resource
                Resource resource = builder().context().resource();
                Resource newResource = builder().context().newResource();
                // default predicate is the name of the element class
                String predicate = element.getClass().getSimpleName();

                // the _predicate field allows to select a field to name the resource by a coded value
                if (element.getSettings().containsKey("_predicate")) {
                    predicate = (String)element.getSettings().get("_predicate");
                }
                // put all found fields with configured subfield names to this resource
                for (Field field : fields) {
                    // is there a subield value decoder?
                    Map.Entry<String, Object> me = SubfieldValueMapper.map(subfields, field);
                    if (me.getKey() != null) {
                        String v = me.getValue().toString();
                        if (element.getSettings().containsKey(me.getKey())) {
                            Map<String,Object> vm = (Map<String,Object>)element.getSettings().get(me.getKey());
                            v = vm.containsKey(v) ? vm.get(v).toString() : v;
                        }
                        // is this the "resource type" field or a simple value?
                        if (me.getKey().equals(predicate)) {
                            predicate = v;
                        } else {
                            newResource.add(me.getKey(), v);
                        }
                    } else {
                        // no decoder, simple add field data
                        String property = (String)subfields.get(field.subfieldId());
                        if (property == null) {
                            property = field.subfieldId(); // unmapped subfield ID
                        }
                        newResource.add(property, field.data());
                    }
                    element.field(builder(), field, value);
                }
                // add child resource
                resource.add(predicate, newResource);
                builder().context().setResource(resource); // switch back to old resource
            }
        } else {
            if (detectUnknownKeys) {
                unknownKeys.add(key);
                if (logger.isDebugEnabled()) {
                    logger.debug("unknown key detected: {} {}", fields, value);
                }
            }
        }
        builder().build(element, fields, value);
    }

}
