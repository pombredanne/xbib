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

import org.xbib.date.DateUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class License extends Holding {

    private final String id;

    private final String parent;

    private final String isil;

    private final List<Integer> dates;

    private final Map<String, Object> info;

    public License(Map<String, Object> m) {
        super(m);
        this.id = getString("ezb:license_entry_id");
        this.parent = getString("ezb:zdbid");
        this.isil = getString("ezb:getISIL");
        this.dates = buildDateArray();
        this.info = buildInfo();
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

    public Map<String, Object> info() {
        return info;
    }

    public String mediaType() {
        return "computer";
    }

    public List<Integer> dates() {
        return dates;
    }

    private List<Integer> buildDateArray() {
        List<Integer> dates = new ArrayList();
        String firstDate = getString("ezb:license_period.ezb:first_date");
        int first;
        if (firstDate != null) {
            first = Integer.parseInt(firstDate);
            String lastDate = getString("ezb:license_period.ezb:last_date");
            int last;
            last = lastDate == null ? DateUtil.getYear() : Integer.parseInt(lastDate);
            if (first > 0 && last > 0) {
                for (int d = first; d <= last; d++) {
                    dates.add(d);
                }
            }
        }
        // TODO moving wall
            /*String movingWall = getString("ezb:license_period.ezb:moving_wall");
            if (logger.isDebugEnabled()) {
                logger.debug("buildDateArray: {} --> {}, movingwall {}", map(), dates, movingWall);
            }*/
        return dates;
    }

    private Map<String, Object> buildInfo() {
        Map<String, Object> m = new LinkedHashMap();
        Map<String, Object> period = new LinkedHashMap();
        period.put("firstvolume", getString("ezb:license_period.ezb:first_volume"));
        period.put("firstdate", getString("ezb:license_period.ezb:first_date"));
        period.put("lastvolume", getString("ezb:license_period.ezb:last_volume"));
        period.put("lastdate", getString("ezb:license_period.ezb:last_date"));
        period.put("available", getString("ezb:license_period.ezb:available"));
        m.put("period", period);
        m.put("type", map().get("ezb:type_id"));
        m.put("license", map().get("ezb:license_type_id"));
        m.put("price", map().get("ezb:price_type_id"));
        m.put("url", map().get("ezb:reference_url"));
        m.put("readme", map().get("ezb:readme_url"));
        Map<String, Object> service = new LinkedHashMap();
        service.put("servicecomment", getString("ezb:ill_relevance.ezb:comment"));
        String s = getString("ezb:ill_relevance.ezb:inland_only");
        String t = getString("ezb:ill_relevance.ezb:il_electronic_forbidden");
        if ("true".equals(s) && "true".equals(t)) {
            service.put("servicedistribution", Arrays.asList("domestic-only", "postal-only"));
        } else if ("true".equals(s)) {
            service.put("servicedistribution", "domestic-only");
        } else if ("true".equals(t)) {
            service.put("servicedistribution", "postal-only");
        }
        service.put("servicemode", getString("ezb:ill_relevance.ezb:ill_code"));
        m.put("service", service);
        return m;
    }

}
