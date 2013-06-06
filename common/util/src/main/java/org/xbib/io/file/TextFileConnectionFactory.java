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
package org.xbib.io.file;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import org.xbib.io.Connection;
import org.xbib.io.ConnectionFactory;
import org.xbib.io.InputStreamFactory;
import org.xbib.io.StreamCodecService;

/**
 * <p>A file connection service</p>
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante
 */
public final class TextFileConnectionFactory
        implements ConnectionFactory<TextFileSession>, InputStreamFactory<InputStream> {

    private StreamCodecService service = StreamCodecService.getInstance();

    @Override
    public Connection<TextFileSession> getConnection(URI uri) throws IOException {
        TextFileConnection connection = new TextFileConnection();
        connection.setURI(uri);
        return connection;
    }

    @Override
    public boolean providesScheme(String scheme) {
        return scheme.startsWith("file");
    }

    @Override
    public InputStream getInputStream(URI uri) throws IOException {
        String scheme = uri.getScheme();
        if (!providesScheme(uri.getScheme())) {
            return null;
        }
        String path = uri.getSchemeSpecificPart();
        InputStream in = new FileInputStream(path);
        if (scheme.endsWith("gz") || path.endsWith(".gz")) {
            return service.getCodec("gz").decode(in);
        } else if (scheme.endsWith("bz2") || path.endsWith(".bz2")) {
            return service.getCodec("bz2").decode(in);
        } else if (scheme.endsWith("xz") || path.endsWith(".xz")) {
            return service.getCodec("xz").decode(in);
        }
        return in;
    }
}
