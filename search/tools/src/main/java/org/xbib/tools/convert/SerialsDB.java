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
package org.xbib.tools.convert;

import org.xbib.csv.CSVParser;
import org.xbib.grouping.bibliographic.work.PublishedJournal;
import org.xbib.io.file.Finder;
import org.xbib.io.file.TextFileConnectionFactory;
import org.xbib.iri.IRI;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import org.xbib.rdf.Resource;
import org.xbib.rdf.context.IRINamespaceContext;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.simple.SimpleResourceContext;
import org.xbib.standardnumber.ISSN;
import org.xbib.standardnumber.InvalidStandardNumberException;
import org.xbib.tools.opt.OptionParser;
import org.xbib.tools.opt.OptionSet;

import java.io.EOFException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.TreeMap;
import java.util.Map;
import java.util.Queue;

/**
 * Import serials list
 *
 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class SerialsDB {

    private final static Logger logger = LoggerFactory.getLogger(SerialsDB.class.getName());

    private final static String lf = System.getProperty("line.separator");

    private final static TextFileConnectionFactory factory = new TextFileConnectionFactory();

    private static Queue<URI> input;

    private final static SimpleResourceContext resourceContext = new SimpleResourceContext();

    private Map<String,Resource> map;

    public static void main(String[] args) {
        int exitcode = 0;
        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("input").withRequiredArg().ofType(String.class).required();
                    accepts("pattern").withRequiredArg().ofType(String.class).required().defaultsTo("titleFile.csv");
                    accepts("output").withRequiredArg().ofType(String.class).required().defaultsTo("serials");
                    accepts("help");
                }
            };
            final OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Help for " + ArticleDB.class.getCanonicalName() + lf
                        + " --help                 print this help message" + lf
                        + " --input <path>         a file path from where the input files are collected (required)" + lf
                        + " --pattern <pattern>    a regex for selecting matching file names for input (default: titleFile.csv)" + lf
                        + " --output <path>        a file base name from where the output is written (default: serials)" + lf
                );
                System.exit(1);
            }
            input = new Finder(options.valueOf("pattern").toString()).find(options.valueOf("input").toString()).getURIs();

            SerialsDB serialsdb = null;
            for (URI uri : input) {
                InputStream in = factory.getInputStream(uri);
                String output = (String)options.valueOf("output");
                serialsdb = new SerialsDB(new InputStreamReader(in, "UTF-8"), output);
                serialsdb.writeSerials(new FileWriter(output + ".txt"));
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            exitcode = 1;
        }
        System.exit(exitcode);
    }

    public SerialsDB(Reader input, String outputFile) throws IOException {

        String fileName = outputFile + ".ttl";
        FileWriter w = new FileWriter(fileName);

        map = new TreeMap();

        IRINamespaceContext context = IRINamespaceContext.newInstance();
        context.addNamespace("dc", "http://purl.org/dc/elements/1.1/");
        context.addNamespace("prism", "http://prismstandard.org/namespaces/basic/2.1/");
        resourceContext.newNamespaceContext(context);
        final TurtleWriter writer = new TurtleWriter()
                .setContext(context)
                .output(w);

        CSVParser parser = new CSVParser(input);
        try {
            int i = 0;
            while (true) {
                String journalTitle = parser.nextToken().trim();
                String publisher = parser.nextToken().trim();
                String subjects = parser.nextToken();
                String issn = parser.nextToken().trim();
                String doi = parser.nextToken().trim();
                String publicationStructure = parser.nextToken();
                String[] issnArr = issn.split("\\|");
                // skip fake titles
                if ("xxxx".equals(journalTitle)) {
                    continue;
                }
                if (i > 0) {
                    Resource resource = resourceContext.newResource();
                    String issn1 = buildISSN(issnArr, 0);
                    String issn2 = buildISSN(issnArr, 1);
                    try {
                        if (issn1 != null) {
                            new ISSN(issn1);
                        }
                        if (issn2 != null) {
                            new ISSN(issn2);
                        }
                        if (issn1 != null && issn1.equals(issn2)) {
                            issn2 = null;
                        }
                        String id = new PublishedJournal()
                                .journalName(journalTitle)
                                .publisherName(publisher)
                                .createIdentifier();
                        // journals are not "works", they are endeavors
                        IRI iri = IRI.builder().scheme("http")
                                        .host("xbib.info")
                                        .path("/endeavors/" + id)
                                        .build();
                        resource.id(iri)
                            .add("dc:publisher", publisher.isEmpty() ? null : publisher)
                            .add("dc:title", journalTitle)
                            .add("prism:issn", issn1)
                            .add("prism:issn", issn2)
                            .add("prism:doi", doi.isEmpty() ? null : doi);
                        if (!map.containsKey(journalTitle)) {
                            writer.write(resource);
                            map.put(journalTitle, resource);
                        } else {
                            logger.info("ignoring double serial title: {}", journalTitle);
                        }
                    } catch (InvalidStandardNumberException ex) {
                        // skip fake ISSN titles
                    }
                }
                i++;
            }
        } catch (EOFException e) {
            // ignore
        }
    }

    public Map<String,Resource> getMap() {
        return map;
    }

    private String buildISSN(String[] issnArr, int i) {
        return issnArr.length > i && !issnArr[i].isEmpty() ?
                new StringBuilder(issnArr[i]).insert(4,'-').toString() : null;
    }

    /**
     * Simple text list of serials, with IRIs
     *
     * @param w
     * @throws IOException
     */
    public void writeSerials(Writer w) throws IOException{
        for (String j : getMap().keySet()) {
            w.write(j + "|" + getMap().get(j));
            w.write("\n");
        }
        w.close();
    }

}
