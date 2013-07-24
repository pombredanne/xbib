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
package org.xbib.query.cql.elasticsearch;

import java.io.IOException;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

public class SourceGenerator {

    private final XContentBuilder builder;

    public SourceGenerator() throws IOException {
        this.builder = XContentFactory.jsonBuilder();
    }

    public void build(QueryGenerator query,
            int from, int size) throws IOException {
        build(query, from, size,  null, null, null);
    }

    public void build(QueryGenerator query, int from, int size,
                      XContentBuilder sort,
                      XContentBuilder filter,
                      XContentBuilder facets
            ) throws IOException {
        builder.startObject();
        builder.field("from", from);
        builder.field("size", size);
        builder.rawField("query", query.getResult().bytes());
        if (filter != null) {
            builder.rawField("filter", filter.bytes() );
        }
        if (sort != null) {
            builder.rawField("sort", sort.bytes());
        }
        if (facets != null) {
            builder.rawField("facets", facets.bytes() );
        }
        builder.endObject();
        builder.close();
    }

    public String getResult() throws IOException {
        return builder.string();
    }
}
