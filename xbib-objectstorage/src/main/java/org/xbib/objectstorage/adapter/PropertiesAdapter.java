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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Hashtable;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import org.xbib.objectstorage.ObjectStorageRequest;
import org.xbib.objectstorage.adapter.container.DefaultContainer;

public class PropertiesAdapter extends AbstractAdapter {

    private final static Logger logger = Logger.getLogger(PropertiesAdapter.class.getName());
    private final Properties properties = new Properties();

    public PropertiesAdapter() {
        this("default");
    }

    public PropertiesAdapter(String name) {
        super();
        InputStream in = PropertiesAdapter.class.getResourceAsStream("/org/xbib/objectstorage/adapter/" + name + ".properties");
        if (in != null) {
            try {
                properties.load(in);
                // create default container
                addContainer(new DefaultContainer(getDefaultContainerName(), "Default Container",
                        ResourceBundle.getBundle(properties.getProperty("container_bundle"))));
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        } else {
            throw new IllegalArgumentException("adapter " + name + " not found");
        }
    }

    @Override
    public DirContext getDirContext() throws NamingException {
        Hashtable<Object, Object> env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, getLDAPInitialContextFactory());
        env.put(Context.PROVIDER_URL, getLDAPProviderURL());
        env.put(Context.SECURITY_AUTHENTICATION, getLDAPSecurityAuthentication());
        env.put(Context.SECURITY_PRINCIPAL, getLDAPSecurityPrincipal());
        env.put(Context.SECURITY_CREDENTIALS, getLDAPSecurityCredentials());
        env.put("_baseDN", getLDAPBaseDN());
        return new InitialDirContext(env);
    }

    @Override
    public ObjectStorageRequest newRequest() throws IOException {
        return new DefaultRequest(this);
    }

    @Override
    public String getDriverClassName() {
        return properties.getProperty("driverClassName");
    }

    @Override
    public String getConnectionSpec() {
        return properties.getProperty("jdbc");
    }

    @Override
    public String getUser() {
        return properties.getProperty("user");
    }

    @Override
    public String getPassword() {
        return properties.getProperty("password");
    }

    @Override
    public String getStatementBundleName() {
        return properties.getProperty("statement_bundle");
    }

    @Override
    public String getRoot() {
        return properties.getProperty("root");
    }

    @Override
    public String getDefaultContainerName() {
        return properties.getProperty("container_name");
    }

    public String getLDAPInitialContextFactory() {
        return properties.getProperty("ldap_initialcontextfactory");
    }

    public String getLDAPProviderURL() {
        return properties.getProperty("ldap_providerurl");
    }

    public String getLDAPSecurityAuthentication() {
        return properties.getProperty("ldap_securityauthentication");
    }

    public String getLDAPSecurityPrincipal() {
        return properties.getProperty("ldap_securityprincipal");
    }

    public String getLDAPSecurityCredentials() {
        return properties.getProperty("ldap_securitycredentials");
    }

    public String getLDAPBaseDN() {
        return properties.getProperty("ldap_basedn");
    }

    @Override
    public URI getAdapterURI() {
        return URI.create(properties.getProperty("uri"));
    }
}
