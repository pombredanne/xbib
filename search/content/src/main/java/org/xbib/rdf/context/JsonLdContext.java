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
package org.xbib.rdf.context;

import org.xbib.iri.IRI;
import org.xbib.rdf.Literal;
import org.xbib.rdf.Property;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleResource;
import org.xbib.rdf.xcontent.ContentBuilder;
import org.xbib.rdf.xcontent.DefaultContentBuilder;

import java.util.Map;

public class JsonLdContext
        extends AbstractResourceContext<Resource>
        implements ResourceContext<Resource> {

    public static final String CONTEXT = "@context";

    public static final String ID = "@id";

    public static final String TYPE = "@type";

    public static final String LANGUAGE = "@language";

    public static final String CONTAINER = "@container";

    private final ContentBuilder contentBuilder = new DefaultContentBuilder();

    @Override
    public Resource newResource() {
        Resource<Resource,Property,Literal> root = new SimpleResource().id(IRI.builder().schemeSpecificPart("@context").build());
        Resource resource = root.newResource(CONTEXT);
        for (Map.Entry<String,String> me : namespaceContext().getNamespaces().entrySet()) {
            resource.add(me.getKey(), me.getValue());
        }
        return root;
    }

    public JsonLdContext id(String name, String id) {
        return this;
    }
    public JsonLdContext type(String name, String type) {
        return this;
    }
    public JsonLdContext language(String name, String language) {
        return this;
    }
    public JsonLdContext container(String name, String container) {
        return this;
    }

    @Override
    public ResourceContext<Resource> prepareForOutput() {
        return this;
    }

    public ContentBuilder contentBuilder() {
        return contentBuilder;
    }

}
