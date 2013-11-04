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
package org.xbib.elasticsearch.tools.aggregate.zdb.entities;

import org.xbib.date.DateUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class License extends Holding {

    public License(Map<String, Object> m) {
        super(m);
        super.id = getString("ezb:license_entry_id");
        this.parent = getString("ezb:zdbid");
        this.isil = getString("ezb:isil");
        this.deleted = "delete".equals(getString("ezb:action"));
        this.dates = buildDateArray();
        this.info = buildInfo();
        this.findContentType();
    }

    @Override
    public Map<String, Object> holdingInfo() {
        return info;
    }

    protected void findContentType() {
        this.mediaType = "computer";
        this.carrierType = "online resource";
    }

    protected List<Integer> buildDateArray() {
        List<Integer> dates = new ArrayList();
        String firstDate = getString("ezb:license_period.ezb:first_date");
        int first;
        int last;
        if (firstDate != null) {
            first = Integer.parseInt(firstDate);
            String lastDate = getString("ezb:license_period.ezb:last_date");
            last = lastDate == null ? DateUtil.getYear() : Integer.parseInt(lastDate);
            if (first > 0 && last > 0) {
                for (int d = first; d <= last; d++) {
                    dates.add(d);
                }
            }
        }
        String movingWall = getString("ezb:license_period.ezb:moving_wall");
        if (movingWall != null) {
            Matcher m = movingWallPattern.matcher(movingWall);
            if (m.matches()) {
                int delta = Integer.parseInt(m.group(1));
                last = DateUtil.getYear();
                first = last - delta;
                if ("+".startsWith(movingWall)) {
                    for (int d = first; d <= last; d++) {
                        dates.add(d);
                    }
                } else if ("-".startsWith(movingWall)) {
                    for (int d = first; d <= last; d++) {
                        dates.remove(d);
                    }
                }
            }
        }
        return dates;
    }

    private final static Pattern movingWallPattern = Pattern.compile("^[+-](\\d+)Y$");

    private Map<String, Object> buildInfo() {
        Map<String, Object> m = new LinkedHashMap();

        // location, marcorg, region not present...

        Map<String, Object> service = new LinkedHashMap();
        service.put("organization", getString("ezb:isil") );
        /*
                        case "n" : return "nein";
                        case "l" : return "ja, Leihe und Kopie";
                        case "k" : return "ja, nur Kopie";
                        case "e" : return "ja, auch elektronischer Versand an Nutzer";
                        case "ln" : return "ja, Leihe und Kopie (nur Inland)";
                        case "kn" : return "ja, nur Kopie (nur Inland)";
                        case "en" : return "ja, auch elektronischer Versand an Nutzer (nur Inland)";

         */
        String servicemode = getString("ezb:ill_relevance.ezb:ill_code");
        if (servicemode != null) {
            switch(servicemode) {
                case "nein":
                {
                    service.put("servicetype", "none");
                    break;
                }
                case "ja, Leihe und Kopie":
                case "ja, Leihe und Kopie (nur Inland)":
                {
                    service.put("servicetype", "interlibraryloan");
                    service.put("servicemode", "copy-loan");
                    break;
                }
                case "ja, nur Kopie":
                case "ja, nur Kopie (nur Inland)":
                {
                    service.put("servicetype", "interlibraryloan");
                    service.put("servicemode", "copy");
                    break;
                }
                case "ja, auch elektronischer Versand an Nutzer":
                case "ja, auch elektronischer Versand an Nutzer (nur Inland)":
                {
                    service.put("servicetype", "interlibraryloan");
                    service.put("servicemode", "copy-electronic");
                    break;
                }
            }
        }
        String s = getString("ezb:ill_relevance.ezb:inland_only");
        String t = getString("ezb:ill_relevance.ezb:il_electronic_forbidden");
        if ("true".equals(s) && "true".equals(t)) {
            service.put("servicedistribution", Arrays.asList("n", "p"));
        } else if ("true".equals(s)) {
            service.put("servicedistribution", "n");
        } else if ("true".equals(t)) {
            service.put("servicedistribution", "p");
        }
        service.put("servicecomment", getString("ezb:ill_relevance.ezb:comment"));
        m.put("service", service);

        // no textualholdings

        Map<String, Object> holdings = new LinkedHashMap();
        holdings.put("firstvolume", getString("ezb:license_period.ezb:first_volume"));
        holdings.put("firstdate", getString("ezb:license_period.ezb:first_date"));
        holdings.put("lastvolume", getString("ezb:license_period.ezb:last_volume"));
        holdings.put("lastdate", getString("ezb:license_period.ezb:last_date"));
        // ezb:available is map or string?
        Object o = getAnyObject("ezb:license_period.ezb:available");
        String avail = o != null ? o.toString() : null;
        holdings.put("available", avail);
        m.put("holdings", holdings);

        Map<String, Object> link = new LinkedHashMap();
        link.put("uri", map().get("ezb:reference_url"));
        link.put("nonpublicnote", "Verlagsangebot"); // ZDB = "Volltext"
        m.put("links", Arrays.asList(link));

        Map<String, Object> license = new LinkedHashMap();
        license.put("type", map().get("ezb:type_id"));
        license.put("licensetype", map().get("ezb:license_type_id"));
        license.put("pricetype", map().get("ezb:price_type_id"));
        license.put("readme", map().get("ezb:readme_url"));
        m.put("license", license);

        return m;
    }

}
