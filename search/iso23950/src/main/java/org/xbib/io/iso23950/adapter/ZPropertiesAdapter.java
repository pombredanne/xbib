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
package org.xbib.io.iso23950.adapter;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ZPropertiesAdapter extends ZBaseAdapter {
    
    public final static String ENCODING = "encoding";
    public final static String STYLESHEET = "stylesheet";
    public final static String FORMAT = "format";
    public final static String TYPE = "type";
    public final static String ADAPTER_URI = "uri";
    public final static String DATABASE = "database";
    public final static String SYNTAX = "syntax";
    public final static String TIMEOUT = "timeout";
    
    private Properties properties;

    public ZPropertiesAdapter(Properties properties) {
        this.properties = properties;
    }
    
    @Override
    protected String getEncoding() {
        return properties.getProperty(ENCODING, "ANSEL");
    }

    @Override
    protected String getStylesheet() {
        return properties.getProperty(STYLESHEET);
    }

    @Override
    protected String getFormat() {
        return properties.getProperty(FORMAT, "MARC21");
    }

    @Override
    protected String getType() {
        return properties.getProperty(TYPE, "Bibliographic");
    }

    @Override
    public URI getURI() {
        return URI.create(properties.getProperty(ADAPTER_URI));
    }

    @Override
    public List<String> getDatabases() {
        return Arrays.asList(properties.getProperty(DATABASE));
    }

    @Override
    public String getPreferredRecordSyntax() {
        return properties.getProperty(SYNTAX, "marc21");
    }
    
    @Override
    public int getTimeout() {
        return Integer.parseInt(properties.getProperty(TIMEOUT, "30"));
    }
    
}
