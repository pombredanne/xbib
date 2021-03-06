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
package org.xbib.objectstorage.request;

import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.objectstorage.Parameter;
import org.xbib.objectstorage.Request;
import org.xbib.objectstorage.adapter.AbstractAdapter;
import org.xbib.objectstorage.adapter.LDAPUserAttributes;
import org.xbib.objectstorage.adapter.UserAttributes;

import javax.naming.NamingException;
import java.io.IOException;

public class DefaultRequest extends AbstractRequest
        implements Request, Parameter {

    private final static Logger logger = LoggerFactory.getLogger(DefaultRequest.class.getName());
    private LDAPUserAttributes userAttr;

    public DefaultRequest(AbstractAdapter adapter) {
        this.adapter = adapter;
        this.addStringParameter(AUTHORITY_PARAMETER, adapter.getAdapterURI().getUserInfo());
    }

    @Override
    public UserAttributes getUserAttributes() throws IOException {
        if (userAttr == null) {
            try {
                this.userAttr = new LDAPUserAttributes(adapter.getDirContext(), getUser());
            } catch (NamingException ex) {
                logger.error(null, ex);
                throw new IOException(ex);
            }
        }
        return userAttr;
    }
}
