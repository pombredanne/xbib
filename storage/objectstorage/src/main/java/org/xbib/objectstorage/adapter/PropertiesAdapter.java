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
import org.xbib.objectstorage.Adapter;
import org.xbib.objectstorage.action.sql.SQLService;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.util.Hashtable;
import java.util.Properties;

public abstract class PropertiesAdapter implements Adapter {

    private final static Logger logger = LoggerFactory.getLogger(PropertiesAdapter.class.getName());
    private final Properties properties = new Properties();

    public PropertiesAdapter() {
        this("default");
    }

    public PropertiesAdapter(String name) {
        InputStream in = PropertiesAdapter.class.getResourceAsStream("/org/xbib/objectstorage/service/" + name + ".properties");
        if (in != null) {
            try {
                properties.load(in);
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
            }
        } else {
            throw new IllegalArgumentException("service " + name + " not found");
        }
    }

    @Override
    public URI getAdapterURI() {
        return URI.create(properties.getProperty("uri"));
    }

    @Override
    public DirContext getDirContext() throws NamingException {
        Hashtable<Object, Object> env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, properties.getProperty("ldap_initialcontextfactory"));
        env.put(Context.PROVIDER_URL,  properties.getProperty("ldap_providerurl"));
        env.put(Context.SECURITY_AUTHENTICATION, properties.getProperty("ldap_securityauthentication"));
        env.put(Context.SECURITY_PRINCIPAL, properties.getProperty("ldap_securityprincipal"));
        env.put(Context.SECURITY_CREDENTIALS, properties.getProperty("ldap_securitycredentials"));
        env.put("ldap_basedn",  properties.getProperty("ldap_basedn"));
        return new InitialDirContext(env);
    }

    @Override
    public SQLService getSQLService() throws IOException {
        SQLService service = new SQLService();
        try {
            Connection connection = service.getConnection(
                    properties.getProperty("driverClassName"),
                    properties.getProperty("jdbc"),
                    properties.getProperty("user"),
                    properties.getProperty("password"));
            service.setConnection(connection);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new IOException(ex);
        }
        return service;
    }

    public String getBundleName() {
        return properties.getProperty("statement_bundle");
    }
}

