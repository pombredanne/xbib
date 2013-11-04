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
package org.xbib.objectstorage.adapter;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.HashMap;
import java.util.Map;

public class LDAPUserAttributes implements UserAttributes {

    private final static Logger logger = LoggerFactory.getLogger(LDAPUserAttributes.class.getName());

    private final DirContext context;

    private final String user;

    private final Map<String, String> attributes;

    public LDAPUserAttributes(DirContext context, String user) throws NamingException {
        this.context = context;
        this.user = user;
        this.attributes = makeAttributes();
        logger.debug("LDAP user attributes: user = {} attrs = {}", user, attributes);
    }

    private Map<String,String> makeAttributes() throws NamingException {
        Map<String, String> m = new HashMap();
        String base = (String)context.getEnvironment().get("ldap_basedn");
        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String filter = "(uid=" + user + ")";
        NamingEnumeration<SearchResult> results = context.search(base, filter, sc);
        while (results.hasMore()) {
            SearchResult sr = results.next();
            Attributes attrs = sr.getAttributes();
            NamingEnumeration<? extends Attribute> en = attrs.getAll();
            while (en.hasMore()) {
                Attribute attr = en.next();
                m.put(attr.getID(), attr.get(0).toString());
            }
        }
        return m;
    }

    @Override
    public String getName() {
        return attributes.containsKey("description") ?
                attributes.get("description") : attributes.get("uid");
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }
}
