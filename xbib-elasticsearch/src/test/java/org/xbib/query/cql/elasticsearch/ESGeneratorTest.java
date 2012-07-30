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
package org.xbib.query.cql.elasticsearch;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.query.cql.CQLParser;

/**
 * Test Elasticsearch query language generation

 * @author <a href="mailto:joergprante@gmail.com">J&ouml;rg Prante</a>
 */
public class ESGeneratorTest extends Assert {

    /** the logger */
    private final static Logger logger = Logger.getLogger(ESGeneratorTest.class.getName());
   
    
    @Test
    public void testValid() throws Exception {
        TextProcessor t = new TextProcessor();
        LineProcessor p = new LineProcessor() {

            @Override
            public boolean process(String line) throws Exception {
                String[] s = line.split("\\|");
                CQLParser parser = new CQLParser(new StringReader(s[0]));
                parser.parse();
                ESGenerator generator = new ESGenerator();
                parser.getCQLQuery().accept(generator);
                String q = generator.getQueryResult();
                //logger.log(Level.INFO, "INPUT: {0} DSL: {1} RESULT: {2}", 
                //        new Object[]{s[0], q, generator.getRequestResult()});
                
                //String pq = QueryParserService.parseQuery(q);                
                
                //logger.log(Level.INFO, "ES PARSER: " + pq );                
                //if (s.length > 1) {
                    //assertEquals(s[1], generator.getResult());
                //}
                
                return true;
            }
        };
        t.execute("org/xbib/query/cql/elasticsearch/valid", p);
        assertEquals(t.getErrors(), 0);
    }

    class TextProcessor {

        int ok = 0;
        int errors = 0;

        void execute(String resourcePath, LineProcessor lp) throws Exception {
            try (LineNumberReader lr = new LineNumberReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resourcePath),"UTF-8"))) {
                String line;
                while ((line = lr.readLine()) != null) {
                    if (line.trim().length() > 0 && !line.startsWith("#")) {
                        try {
                            if (lp.process(line)) {
                                ok++;
                            } else {
                                errors++;
                            }
                        } catch (Exception e) {
                            logger.log(Level.WARNING, e.getMessage(), e);
                            errors++;
                        }
                    }
                }
            }
        }

        int getOK() {
            return ok;
        }

        int getErrors() {
            return errors;
        }
    }

    public interface LineProcessor {

        boolean process(String line) throws Exception;
    }
}
