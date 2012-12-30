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

import org.xbib.analyzer.elements.pica.zdb.bib.LibraryAddressProperties;
import org.xbib.elements.Element;
import org.xbib.elements.ElementBuilder;
import org.xbib.elements.bibliographic.ExtraBibliographicProperties;
import org.xbib.elements.dublincore.DublinCoreProperties;
import org.xbib.elements.dublincore.DublinCoreTerms;
import org.xbib.elements.dublincore.DublinCoreTermsProperties;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;

import java.util.Map;

public abstract class PicaElement
        implements Element<FieldCollection, String, PicaBuilder>, 
        DublinCoreProperties, 
        DublinCoreTerms, 
        DublinCoreTermsProperties,
        ExtraBibliographicProperties,
        LibraryAddressProperties {

    protected static final Logger logger = LoggerFactory.getLogger(PicaElement.class.getName());

    protected Map<String,Object> params;
    
    @Override
    public PicaElement setSettings(Map params) {
        this.params = params;
        return this;
    }
    
    @Override
    public Map<String,Object> getSettings() {
        return params;
    }
    
    @Override
    public PicaElement begin() {
        return this;
    }

    @Override
    public PicaElement build(PicaBuilder builder, FieldCollection key, String value) {
        return this;
    }    
    
    /**
     * Process mapped element. Empty by default.
     * @param builder
     * @param fields
     * @param value
     */
    public PicaElement fields(ElementBuilder<FieldCollection, String, PicaElement, PicaContext> builder, FieldCollection fields, String value) {
        return this;
    }

    /**
     * Process mapped element with subfield mappings. Empty by default.
     * @param builder
     * @param field
     * @param subfieldType 
     */
    public PicaElement field(ElementBuilder<FieldCollection, String, PicaElement, PicaContext> builder, Field field, String subfieldType) {
        return this;
    }

    @Override
    public PicaElement end() {
        return this;
    }
    
}
