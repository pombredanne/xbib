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
package org.xbib.oai;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;
import org.xbib.io.AbstractResponse;

public class OAIResponse extends AbstractResponse
        implements XMLEventConsumer {

    private static final Logger logger = Logger.getLogger(OAIResponse.class.getName());
    private static final XMLOutputFactory outputFactory =  XMLOutputFactory.newInstance();
    private XMLEventWriter eventWriter;
    private String errorCode;
    private Date responseDate;
    private long expire;

    public OAIResponse(Writer writer) {
        super(writer);
        try {
            eventWriter = outputFactory.createXMLEventWriter(writer);
        } catch (XMLStreamException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public OAIResponse(OutputStream out, String encoding)
            throws UnsupportedEncodingException {
        super(out, encoding);
        try {
            eventWriter = outputFactory.createXMLEventWriter(out);
        } catch (XMLStreamException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    @Override
    public void write() throws IOException {
    }

    @Override
    public void flush() throws IOException {
        try {
            if (eventWriter != null) {
                eventWriter.flush();
            }
        } catch (XMLStreamException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    @Override
    public void add(XMLEvent xmle) throws XMLStreamException {
        if (eventWriter != null) {
            eventWriter.add(xmle);
        }
    }
    
    public void setError(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getError() {
        return errorCode;
    }

    
    public void setResponseDate(Date date) {
        this.responseDate = date;
    }
    
    public Date getResponseDate() {
        return responseDate;
    }
    
    public void setExpire(long millis) {
        this.expire = millis;
    }
    
    public long getExpire() {
        return expire;
    }
}
