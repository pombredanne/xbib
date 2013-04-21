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
package org.xbib.analyzer.elements.marc.holdings;

import org.xbib.elements.ElementBuilder;
import org.xbib.elements.marc.MARCElement;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;
import org.xbib.rdf.Resource;
import org.xbib.elements.support.EnumerationAndChronology;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class TextualHoldings extends MARCElement {

    private final static TextualHoldings instance = new TextualHoldings();
    private Pattern[] movingwallPatterns;

    public static TextualHoldings getInstance() {
        return instance;
    }

    @Override
    public TextualHoldings setSettings(Map params) {
        this.params = params;
        List<String> movingwalls = (List<String>) params.get("movingwall");
        if (movingwalls != null) {
            Pattern[] p = new Pattern[movingwalls.size()];
            for (int i = 0; i < movingwalls.size(); i++) {
                p[i] = Pattern.compile(movingwalls.get(i));
            }
            setMovingwallPatterns(p);
        }
        return this;
    }

    public void setMovingwallPatterns(Pattern[] p) {
        this.movingwallPatterns = p;
    }

    public Pattern[] getMovingwallPatterns() {
        return this.movingwallPatterns;
    }

    @Override
    public void fields(ElementBuilder builder, FieldCollection fields, String value) {
        for (Field field : fields) {
            builder.context().resource().add("textualholdings", field.data());
            if (field.subfieldId().equals("a")) {
                Resource r = builder.context().resource().newResource("holdings");
                Resource parsedHoldings = EnumerationAndChronology.parse(field.data(), r, getMovingwallPatterns());
                if (!parsedHoldings.isEmpty()) {
                    Set<Integer> dates = EnumerationAndChronology.dates(parsedHoldings);
                    builder.context().resource().add("dates", dates);
                } else {
                    logger.debug("no dates found in field " + field);
                }
            }
        }
    }

}
