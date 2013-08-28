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
package org.xbib.tools.harvest;

import org.xbib.io.Connection;
import org.xbib.io.NullWriter;
import org.xbib.io.Packet;
import org.xbib.io.Session;
import org.xbib.io.archivers.TarConnectionFactory;
import org.xbib.io.archivers.TarSession;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.oai.client.OAIClient;
import org.xbib.oai.client.OAIClientFactory;
import org.xbib.oai.record.ListRecordsRequest;
import org.xbib.oai.record.ListRecordsResponseListener;
import org.xbib.oai.util.xml.XmlMetadataHandler;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.text.Normalizer;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import static org.xbib.tools.opt.util.DateConverter.datePattern;

/**
 * Harvest from DNB OAI. Write records to tar archive.
 */
public class DNB {

    private final static Logger logger = LoggerFactory.getLogger(DNB.class.getSimpleName());

    private static OptionSet options;

    private OAIClient client;

    private final static AtomicLong counter = new AtomicLong(0L);

    private TarSession session;

    public static void main(String[] args) throws Exception {
        int exitcode = 0;
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("server").withRequiredArg().ofType(String.class).required();
                    accepts("set").withRequiredArg().ofType(String.class);
                    accepts("prefix").withRequiredArg().ofType(String.class);
                    accepts("from").withRequiredArg().withValuesConvertedBy(datePattern("yyyy-MM-dd'T'hh:mm:ss'Z'"));
                    accepts("until").withRequiredArg().withValuesConvertedBy(datePattern("yyyy-MM-dd'T'hh:mm:ss'Z'")).defaultsTo(new Date());
                    accepts("fromDate").withRequiredArg().withValuesConvertedBy(datePattern("yyyy-MM-dd"));
                    accepts("untilDate").withRequiredArg().withValuesConvertedBy(datePattern("yyyy-MM-dd")).defaultsTo(new Date());
                    accepts("output").withOptionalArg().ofType(String.class).defaultsTo("dnb.xml");
                }
            };
            options = parser.parse(args);

            String server = (String) options.valueOf("server");
            String prefix = (String) options.valueOf("prefix");
            String set = (String) options.valueOf("set");
            Date from = options.valueOf("fromDate") != null?
                    (Date) options.valueOf("fromDate") :  (Date) options.valueOf("from");
            Date until = options.valueOf("untilDate") != null?
                    (Date) options.valueOf("untilDate") :  (Date) options.valueOf("until");
            String output = (String) options.valueOf("output");

            final OAIClient client = OAIClientFactory.newClient(server);

            ListRecordsRequest request = client.newListRecordsRequest()
                    .setMetadataPrefix(prefix)
                    .setSet(set)
                    .setFrom(from)
                    .setUntil(until);

            new DNB(client, output).execute(request).close();

            logger.info("harvested {} documents", counter.get());

        } catch (IOException | InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            exitcode = 1;
        }
        System.exit(exitcode);
    }

    private DNB(OAIClient client, String output) throws Exception {
        this.client = client;
        TarConnectionFactory factory = new TarConnectionFactory();
        Connection<TarSession> connection = factory.getConnection(URI.create("targz:" + output));
        this.session = connection.createSession();
        session.open(Session.Mode.WRITE);
    }

    private DNB execute(ListRecordsRequest request) throws Exception {
        final XmlMetadataHandler metadataHandler = new PacketHandler()
                .setWriter(new StringWriter());
        try {
            do {
                ListRecordsResponseListener listener = new ListRecordsResponseListener(request)
                        .register(metadataHandler);
                request.prepare().execute(listener).waitFor();
                if (listener.getResponse() != null) {
                    NullWriter w = new NullWriter();
                    listener.getResponse().to(w);
                }
                request = listener.isFailure() ? null :
                        client.resume(request, listener.getResumptionToken());
            } while (request != null);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return this;
    }

    private void close() throws IOException {
        client.close();
        session.close();
    }

    class PacketHandler extends XmlMetadataHandler {

        public void endDocument() throws SAXException {
            super.endDocument();
            logger.info("got XML document {}", getIdentifier());
            try {
                Packet p = session.newPacket();
                p.name(getIdentifier());
                String s = getWriter().toString();
                s = Normalizer.normalize(s, Normalizer.Form.NFC);
                p.packet(s);
                session.write(p);
                counter.incrementAndGet();
            } catch (IOException e) {
                throw new SAXException(e);
            }
            setWriter(new StringWriter());
        }
    }
}
