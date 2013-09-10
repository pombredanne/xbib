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
package org.xbib.oai.client;

import org.xbib.date.DateUtil;
import org.xbib.io.Connection;
import org.xbib.io.Packet;
import org.xbib.io.Session;
import org.xbib.io.archivers.TarConnectionFactory;
import org.xbib.io.archivers.TarSession;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.record.ListRecordsRequest;
import org.xbib.oai.record.ListRecordsResponseListener;
import org.xbib.oai.util.xml.XmlMetadataHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;

/**
 * NatLiz client test
 *
 */
public class NatLizClientTest {

    private final Logger logger = LoggerFactory.getLogger(NatLizClientTest.class.getName());

    private TarSession session;

    public void testListRecordsNatLiz() throws Exception {

        OAIClient client = OAIClientFactory.newClient("http://dl380-47.gbv.de/oai/natliz/");
        ListRecordsRequest request = client.newListRecordsRequest()
                .setFrom(DateUtil.parseDateISO("2000-01-01T00:00:00Z"))
                .setUntil(DateUtil.parseDateISO("2014-01-01T00:00:00Z"))
                .setMetadataPrefix("extpp2"); // extpp, extpp2, oai_dc, mods, marcxml, telap, mab, mab_opc

        TarConnectionFactory factory = new TarConnectionFactory();
        Connection<TarSession> connection = factory.getConnection(URI.create("targz:natliz-extpp2"));
        session = connection.createSession();
        session.open(Session.Mode.WRITE);

        final XmlMetadataHandler metadataHandler = new NatLizHandler()
                .setWriter(new StringWriter());

        try {
            do {
                ListRecordsResponseListener listener = new ListRecordsResponseListener(request)
                        .register(metadataHandler);
                request.prepare().execute(listener).waitFor();
                if (listener.getResponse() != null) {
                    StringWriter sw = new StringWriter();
                    listener.getResponse().to(sw);
                    //logger.info("response from NatLiz = {}", sw);
                }
                request = listener.isFailure() ? null :
                        client.resume(request, listener.getResumptionToken());
            } while (request != null);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    class NatLizHandler extends XmlMetadataHandler {

        public void endDocument() throws SAXException {
            super.endDocument();
            logger.info("got XML document {}", getIdentifier());
            try {
                Packet p = session.newPacket();
                p.name(getIdentifier());
                p.packet(getWriter().toString());
                session.write(p);
                //FileWriter fw = new FileWriter("target/" + getIdentifier() + ".xml");
                //fw.write(getWriter().toString());
                //fw.close();
            } catch (IOException e) {
                throw new SAXException(e);
            }
            setWriter(new StringWriter());
        }
    }

}
