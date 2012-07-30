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
package org.xbib.xml.transform;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class TransformerURIResolver implements URIResolver {

    
    
    private String[] path;

    public TransformerURIResolver() {
        this.path = new String[0];
    }

    public TransformerURIResolver(String... path) {
        this.path = path;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String systemId = href;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream in = cl.getResourceAsStream(href);
        if (in == null && path.length > 0) {
            for (String s : path) {
                systemId = s + "/" + href;
                in = cl.getResourceAsStream(systemId);
                if (in != null) {
                    break;
                }
                try {
                    in = new FileInputStream(systemId);
                } catch (FileNotFoundException e) {
                }
                if (in != null) {
                    break;
                }
            }
        }
        if (in == null) {
            throw new TransformerException("href could not be resolved: " + href);
        }
        XMLReader reader; 
        try {
            reader = XMLReaderFactory.createXMLReader();
        } catch (SAXException ex) {
            throw new TransformerException("no XML reader for SAX source in URI resolving for:" + href);
        }
        SAXSource source = new SAXSource(reader, new InputSource(in));
        // set system ID (where to find it)        
        String s = cl.getResource(systemId) != null ? 
                  cl.getResource(systemId).toExternalForm()
                : base != null ? URI.create(base).resolve(systemId).toASCIIString() : systemId;
        source.setSystemId(s);
        return source;
    }
}
