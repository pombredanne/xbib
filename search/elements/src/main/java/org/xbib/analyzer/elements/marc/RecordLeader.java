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
package org.xbib.analyzer.elements.marc;

import org.xbib.elements.ElementBuilder;
import org.xbib.elements.marc.MARCContext;
import org.xbib.elements.marc.MARCElement;
import org.xbib.marc.FieldCollection;

import java.util.Map;

public class RecordLeader extends MARCElement {
    private final static RecordLeader instance = new RecordLeader();
    
    public static MARCElement getInstance() {
        return instance;
    }

    private Map<String,Object> codes;

    private String predicate;

    @Override
    public MARCElement setSettings(Map params) {
        super.setSettings(params);
        this.codes= (Map<String,Object>)params.get("codes");
        this.predicate = params.containsKey("_predicate") ?
                        (String)params.get("_predicate") : "leader";
        return this;
    }

    @Override
    public void fields(ElementBuilder<FieldCollection, String, MARCElement, MARCContext> builder, FieldCollection fields, String value) {
        if (codes == null) {
            return;
        }
        for (String k : codes.keySet()) {
            int pos = Integer.parseInt(k);
            Map<String,String> v = (Map<String,String>)codes.get(k);
            String code = value.length() > pos ? value.substring(pos,pos+1) : "";
            if (v.containsKey(code)) {
                builder.context().resource().add(predicate, v.get(code));
            }
        }
        char ch6 = value.charAt(6);
        char ch7 = value.charAt(7);

        boolean isBook = (ch6 == 'a' || ch6 == 't') &&
                (ch7 == 'a' || ch7 == 'c' || ch7 == 'd' || ch7 == 'm');
        if (isBook) {
            builder.context().resource().add("type", "book");
        }

        boolean isComputerFile = ch6 == 'm';
        if (isComputerFile) {
            builder.context().resource().add("type", "computerfile");
        }

        boolean isMap = (ch6 == 'e' || ch6 == 'f');
        if (isMap) {
            builder.context().resource().add("type", "map");
        }

        boolean isMusic = (ch6 == 'c' || ch6 == 'd' || ch6 == 'i' || ch6 == 'j');
        if (isMusic) {
            builder.context().resource().add("type", "music");
        }

        boolean isContinuingResource = ch6 == 'a' &&
                (ch7 == 'b' || ch7 == 'i' || ch7 == 's');
        if (isContinuingResource) {
            builder.context().resource().add("type", "continuingresource");
        }

        boolean isVisualMaterial = (ch6 == 'g' || ch6 == 'k' || ch6 == 'o' || ch6 == 'r');
        if (isVisualMaterial) {
            builder.context().resource().add("type", "visualmaterial");
        }

        boolean isMixedMaterial = ch6 == 'p';
        if (isMixedMaterial) {
            builder.context().resource().add("type", "mixedmaterial");
        }
    }
}
