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
package org.xbib.tools.aggregator.elasticsearch.zdb.entities;

import org.xbib.map.MapBasedAnyObject;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Holding extends MapBasedAnyObject {

    private final String id;

    private final String parent;

    private String subisil;

    private String isil;

    private String mediaType;

    private Map<String, Object> info;

    public Holding(Map<String, Object> m) {
        super(m);
        this.id = getString("identifierRecord");
        this.parent = getString("identifierParent");
        Object o = map().get("Location");
        if (o instanceof Map) {
            Map<String, Object> map = (Map) o;
            this.subisil = (String) map.get("marcorg");
            if (subisil != null) {
                // cut from last '-' if there is more than one '-'
                int firstpos = subisil.indexOf('-');
                int lastpos = subisil.lastIndexOf('-');
                this.isil = lastpos > firstpos ? subisil.substring(0, lastpos) : subisil;
            }
        } else if (o instanceof List) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) o;
            for (Map<String, Object> map : list) {
                if (map.containsKey("marcorg")) {
                    this.isil = (String) map.get("marcorg");
                }
            }
        }
        if (isil == null) {
            // e.g. DNB-ID 036674168 WEU GB-LON63
            this.subisil = getString("service.organization"); // Sigel
            this.isil = this.subisil;
        }
        // isil may be null, broken holding record, e.g. DNB-ID 114091315 ZDB-ID 2476016x
        //logger.error("no ISIL in " + id);
        if (isil != null) {
            findMediaType();
            this.info = buildInfo();
        }
    }

    public String id() {
        return id;
    }

    public String parent() {
        return parent;
    }

    public String getISIL() {
        return isil;
    }

    public String getSubISIL() {
        return subisil;
    }

    public Map<String,Object> info() {
        return info;
    }

    public String mediaType() {
        return mediaType;
    }

    protected void findMediaType() {
        this.mediaType = "unmediated";
        Object o = map().get("textualholdings");
        if (!(o instanceof List)) {
            o = Arrays.asList(o);
        }
        for (String s : (List<String>) o) {
            if (s != null && s.indexOf("Microfiche") >= 0) {
                this.mediaType = "microform";
                //this.parent = this.parent() + "_microform";
                return;
            }
            if (s != null && (s.indexOf("CD-ROM") >= 0 || s.indexOf("CD-Rom") >= 0)) {
                this.mediaType = "computer optical disc";
                return;
            }
        }
        // rare
        String s = getString("SourceOfAcquisition.accessionNumber");
        if (s != null) {
            if (s.indexOf("Microfiche") >= 0) {
                this.mediaType = "microform";
                //this.parent = this.parent() + "_microform";
                return;
            }
        }
    }

    private Map<String, Object> buildInfo() {
        Map<String, Object> m = new LinkedHashMap();
        m.put("mediaType", mediaType());
        m.put("location", map().get("Location"));
        m.put("service", map().get("service"));
        m.put("textualholdings", map().get("textualholdings"));
        m.put("holdings", map().get("holdings"));
        /**
         * [
         {
         "uri": "http://www.bibliothek.uni-regensburg.de/ezeit/?2002503&bibid=FHBKA",
         "nonpublicnote": "EZB"
         },
         {
         "uri": "http://search.ebscohost.com/direct.asp?db=afh&jid=EHM&scope=site",
         "nonpublicnote": "Volltext"
         }
         ]
         */
        m.put("links", map().get("ElectronicLocationAndAccess"));
        /**
         *  "license": {
         "originSource": "a",
         "origin": "EZB",
         "typeSource": "c",
         "type": "self-hosted",
         "scopeSource": "b",
         "scope": "consortia",
         "chargeSource": "c",
         "charge": "yes"
         },
         */
        m.put("license", map().get("license"));
        return m;
    }
}
