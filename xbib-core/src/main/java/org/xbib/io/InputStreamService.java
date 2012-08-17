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
package org.xbib.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Iterator;
import java.util.ServiceLoader;

public class InputStreamService {

    public static int DEFAULT_BUFFER_SIZE = 8192;

    private InputStreamService() {
    }

    public static InputStream getInputStream(URI uri) throws IOException {
        InputStreamFactory factory;
        ServiceLoader<InputStreamFactory> loader = ServiceLoader.load(InputStreamFactory.class);
        Iterator<InputStreamFactory> it = loader.iterator();
        while (it.hasNext()) {
            factory = it.next();
            if (uri.getScheme() != null && factory.providesScheme(uri.getScheme())) {
                return factory.getInputStream(uri);
            }
        }
        return null;
    }

    public static String getString(InputStream input, String encoding) throws IOException {
        return getString(new InputStreamReader(input,encoding));
    }
    
    public static String getString(Reader input) throws IOException {
        StringWriter output = new StringWriter();
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int n;
        while ((n = input.read(buffer))!= -1) {
            output.write(buffer, 0, n);
        }
        return output.toString();
    }
}
