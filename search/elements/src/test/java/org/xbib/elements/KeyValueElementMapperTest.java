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
package org.xbib.elements;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.elements.marc.MARCSpecification;
import org.xbib.elements.marc.extensions.mab.MABSpecification;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;

public class KeyValueElementMapperTest extends Assert {

    private final Logger logger = LoggerFactory.getLogger(KeyValueElementMapperTest.class.getName());

    @Test
    public void testMARCSpecs() {
        String value = "100$0$1$abc";
        Element element = new NullElement();
        Map map = new TreeMap(); // for sorted output in assertEquals matching
        AbstractSpecification specification = new MARCSpecification();
        Map m = specification.addSpec(value, element, map);
        value = "100$0$2$abc";
        element = new NullElement();
        m = specification.addSpec(value, element, m);
        value = "100$0$2$def";
        element = new NullElement();
        m = specification.addSpec(value, element, m);
        value = "200$0$2$abc";
        element = new NullElement();
        m = specification.addSpec(value, element, m);
        assertEquals("{100={0={2={abc=<null>, def=<null>}, 1={abc=<null>}}}, 200={0={2={abc=<null>}}}}", m.toString());
        Element e = specification.getElement("100$0$1$abc", m);
        logger.info("e={}", e);
        e = specification.getElement("100$0$1$def", m);
        logger.info("e={}", e);
    }

    @Test
    public void testMABSpecs() {
        String value = "331";
        Element element = new NullElement();
        Map map = new TreeMap(); // for sorting
        AbstractSpecification specification = new MABSpecification();
        Map m = specification.addSpec(value, element, map);
        logger.info("mab spec 1 = {}", m);
        value = "331 10";
        m = specification.addSpec(value, element, map);
        logger.info("mab spec 2 = {}", m);
        value = "[331 1a, 331 19]";
        m = specification.addSpec(value, element, map);
        logger.info("mab spec 3 = {}", m);
    }

}
