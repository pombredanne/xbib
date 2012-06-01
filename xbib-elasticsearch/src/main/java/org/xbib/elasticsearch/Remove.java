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
package org.xbib.elasticsearch;

import java.io.IOException;
import org.elasticsearch.action.delete.DeleteResponse;
import org.xbib.io.Identifiable;
import org.xbib.io.Session;
import org.xbib.rdf.Resource;

/**
 * Remove document from ElasticSearch
 * 
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class Remove extends AbstractWrite {

    public Remove(String index, String type) {
        super(index, type);
    }
    
    @Override
    public void write(ElasticsearchSession session, Resource resource) throws IOException {
        DeleteResponse response = session.getClient().prepareDelete().setIndex(index).setType(type).setId(resource.getIdentifier().getFragment()).execute().actionGet();
    }
    
    @Override
    public void flush(ElasticsearchSession session) {
    }

    @Override
    public void execute(Session session) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void create(ElasticsearchSession session, Identifiable identifiable, Resource resource) throws IOException {
        write(session, resource);
    }
}