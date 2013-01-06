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

import org.xbib.elements.AbstractElementBuilder;
import org.xbib.elements.ElementContextFactory;
import org.xbib.elements.dublincore.DublinCoreProperties;
import org.xbib.elements.output.ElementOutput;
import org.xbib.iri.IRI;
import org.xbib.marc.FieldCollection;
import org.xbib.rdf.Property;

public class MARCBuilder
    extends AbstractElementBuilder<FieldCollection, String, MARCElement, MARCContext>
    implements DublinCoreProperties {
    
    private final ElementContextFactory<MARCContext> contextFactory = new ElementContextFactory<MARCContext>() {

        @Override
        public MARCContext newContext() {
            return new MARCContext();
        }
    };     
    
    @Override
    protected ElementContextFactory<MARCContext> getContextFactory() {
        return contextFactory;
    }
    
    @Override
    public MARCContext context() {
        return context.get();
    }
    
    @Override
    public MARCBuilder addOutput(ElementOutput output) {
        super.addOutput(output);
        return this;
    }
    
    @Override
    public void end() {
        // do nothing at record ends
    }

    @Override
    public void build(MARCElement element, FieldCollection fields, String value) {
        if (context().resource().id() == null) {
            context().resource().id(IRI.create("http://xbib.org#"+context().increment()));
        }
    }
    
    public MARCBuilder elementProperty(String elementName, Property property, String value) {        
        context.get().getResource(context.get().resource(), elementName).add(property, value);
        return this;
    }
    
}
