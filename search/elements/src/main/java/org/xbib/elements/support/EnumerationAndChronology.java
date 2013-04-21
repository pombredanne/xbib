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
package org.xbib.elements.support;

import org.xbib.date.DateUtil;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;
import org.xbib.rdf.Resource;
import org.xbib.rdf.simple.SimpleResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsing patterns of enumeration and chronology
 * <p/>
 * Rules are given by Zeitschriftendatenbank
 * http://support.d-nb.de/iltis/katricht/zdb/8032.pdf
 *
 */
public class EnumerationAndChronology {

    private static final Logger logger = LoggerFactory.getLogger(EnumerationAndChronology.class.getName());

    private final static Pattern[] p1a = new Pattern[] {
            // 1921=1339
            // 1921/22=1339
            Pattern.compile("(\\d{4})(/?\\d{2})\\s*="),
            // 1965/70(1971/72)
            Pattern.compile("(\\d{4})(/?\\d{2})\\((\\d{4})(/?\\d{2})\\)"),
            // 1965/66
            // 1968/70
            // 1961/62(1963)
            // 1961/62(1962)
            // WS 1948/49
            Pattern.compile("(\\d{4})(/?\\d{2})"),
            // 1961
            // 1992,14140(12. März)
            // SS 1922
            Pattern.compile("(\\d{4})")
    };
    private final static Pattern[] p1b = new Pattern[]{
            // 1.1970/71
            // 2.1938/40(1942)
            // 9.1996/97(1997)
            Pattern.compile("(\\d+)\\.(\\d{4})(/\\d{0,4})"),
            // 1.1970
            // 2.1970,3
            // 4.1961,Aug.
            // 3.1971,Jan./Febr.
            // 1.1970; 3.1972; 7.1973
            Pattern.compile("(\\d+)\\.(\\d{4})"),
            // 1.[19]93,1
            Pattern.compile("(\\d+)\\.(\\[?\\d{2}\\]?\\d{2}/?\\d{0,4})")
    };
    private final static Pattern[] p1c = new Pattern[] {
            // An V= [1796/97]
            // 1.5678=[1917/18]
            Pattern.compile("=\\s*\\[(\\d{4})(/?\\d{0,4})\\]"),
            // 1.1981=1401
            Pattern.compile("\\.(\\d{4}/?\\d{0,4})\\s*=")
    };
    private final static Pattern[] p2a = new Pattern[] {
            // 1971 -
            Pattern.compile("(\\d{4}/?\\d{0,4}).*\\-\\s*$"),
            // 1963,21(22.Mai) -
            Pattern.compile("(\\d{4}).*\\-\\s*$"),
            // [19]51,1 - [19]52,5
            Pattern.compile("(\\[\\d{2}\\]\\d{2}).*\\-\\s*$")
    };
    private final static Pattern[] p2b = new Pattern[] {
            // 1.1971 -
            // 2.1947,15.Mai -
            Pattern.compile("(\\d+)\\.(\\d{4}/?\\d{0,4}).*\\-\\s*$")
    };
    private final static Pattern[] p3a = new Pattern[] {
            // 1963 - 1972
            Pattern.compile("(\\d{4}/?\\d{0,4}).*\\-\\s*(\\d{4}/?\\d{0,4})"),
            // [19]51,1 - [19]52,5
            Pattern.compile("(\\[\\d{2}\\]\\d{2}/?\\d{0,4}).*\\-\\s*(\\[\\d{2}\\]\\d{2}/?\\d{0,4})")
    };
    private final static Pattern[] p3b = new Pattern[] {
            // 6.1961/64 - 31.1970
            // 1.1963 - 12.1972
            // 115.1921/22(1923) - 1125.1937
            // 3.1858,6 - 24.1881,3
            // 1.1960 - 5.1963; 11.1964; 23.1971 -
            Pattern.compile("(\\d+)\\.(\\d{4}/?\\d{0,4}).*\\-\\s*(\\d+)\\.(\\d{4}/?\\d{0,4})"),
            // [19]81/82 - [19]83
            Pattern.compile("(\\d+)\\.(\\[\\d{2}\\]\\d{2})(/\\d{0,4}).*\\-\\s*(\\d+)\\.(\\[\\d{2}\\]\\d{2})(/\\d{0,4})"),
            // 1.[19]51,1 - 1.[19]52,5
            Pattern.compile("(\\d+)\\.(\\[\\d{2}\\]\\d{2}).*\\-\\s*(\\d+)\\.(\\[\\d{2}\\]\\d{2})")
    };
    private final static Pattern[] p4a = new Pattern[] {
            // 2.1938/40(1942)
            // 9.1996/97(1997)
            // 115.1921/22(1923) - 1125.1937
            // 1961/62(1963)
            // 1965/70(1971/72)
            Pattern.compile("\\((\\d{4}/?\\d{0,4})\\)")
    };

    private EnumerationAndChronology() {
    }

    public static Resource parse(String values) {
        return parse(values, new SimpleResource(), null);
    }

    public static Resource parse(String values, Resource resource, Pattern[] movingwalls) {
        if (values == null) {
            return resource;
        }
        for (String value : values.split(";")) {
            boolean found = false;
            // first, check dates in parentheses
            for (Pattern p : p4a) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    resource.newResource("group")
                            .add("begindate", sanitizeDate(m.group(1)));
                }
            }
            // always continue - parentheses dates are optional
            for (Pattern p : p3b) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    Integer b = sanitizeDate(m.group(2));
                    Integer e = sanitizeDate(m.group(4));
                    resource.newResource("group")
                            .add("beginvolume", m.group(1))
                            .add("begindate", b)
                            .add("endvolume", m.group(3))
                            .add("enddate", e);
                    break;
                }
            }
            if (found) {
                continue;
            }
            found = false;
            for (Pattern p : p3a) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    if (m.groupCount() == 4) {
                        List<Integer> l1 = fractionDate(m.group(1) + m.group(2));
                        List<Integer> l2 = fractionDate(m.group(3) + m.group(4));
                        Integer b = sanitizeDate(l1.get(0));
                        Integer e = sanitizeDate(l2.get(l2.size()-1));
                        resource.newResource("group")
                                .add("begindate", b)
                                .add("enddate", e);
                    } else {
                        List<Integer> l1 = fractionDate(m.group(1));
                        List<Integer> l2 = fractionDate(m.group(2));
                        Integer b = sanitizeDate(l1.get(0));
                        Integer e = sanitizeDate(l2.get(l2.size()-1));
                        resource.newResource("group")
                                .add("begindate", b)
                                .add("enddate", e);
                    }
                    break;
                }
            }
            if (found) {
                continue;
            }
            found = false;
            for (Pattern p : p2b) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    resource.newResource("group")
                            .add("beginvolume", m.group(1))
                            .add("begindate", sanitizeDate(m.group(2)))
                            .add("open", "true");
                    break;
                }
            }
            if (found) {
                continue;
            }
            found = false;
            for (Pattern p : p2a) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    resource.newResource("group")
                            .add("begindate", sanitizeDate(m.group(1)))
                            .add("open", "true");
                    break;
                }
            }
            if (found) {
                continue;
            }
            found = false;
            for (Pattern p : p1c) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    if (m.groupCount() > 1) {
                        List<Integer> l = fractionDate(m.group(1) + m.group(2));
                        Integer b = sanitizeDate(l.get(0));
                        resource.newResource("group")
                                .add("begindate", b );
                        if (l.size() > 1) {
                            b = sanitizeDate(l.get(1));
                            resource.newResource("group")
                                .add("begindate", b);
                        }
                    } else {
                        resource.newResource("group")
                            .add("begindate", sanitizeDate(m.group(1)));
                    }
                    break;
                }
            }
            if (found) {
                continue;
            }
            found = false;
            for (Pattern p : p1b) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    if (m.groupCount() == 3) {
                        List<Integer> l = fractionDate(m.group(2) + m.group(3));
                        Integer b = sanitizeDate(l.get(0));
                        resource.newResource("group")
                                .add("beginvolume", m.group(1))
                                .add("begindate", b);
                        if (l.size() > 1) {
                            b = sanitizeDate(l.get(1));
                            resource.newResource("group")
                                .add("beginvolume", m.group(1))
                                .add("begindate", b );
                        }
                    } else {
                        resource.newResource("group")
                            .add("beginvolume", m.group(1))
                            .add("begindate", sanitizeDate(m.group(2)));
                    }
                    break;
                }
            }
            if (found) {
                continue;
            }
            found = false;
            for (Pattern p : p1a) {
                Matcher m = p.matcher(value);
                found = m.find();
                if (found) {
                    if (m.groupCount() == 4) {
                        List<Integer> l1 = fractionDate(m.group(1) + m.group(2));
                        List<Integer> l2 = fractionDate(m.group(3) + m.group(4));
                        Integer b = sanitizeDate(l1.get(0));
                        resource.newResource("group")
                                .add("begindate", b);
                        if (l1.size() > 1) {
                            b = sanitizeDate(l1.get(1));
                            resource.newResource("group")
                                .add("begindate", b);
                        }
                        b = sanitizeDate(l2.get(0));
                        resource.newResource("group")
                                .add("begindate", b);
                        if (l2.size() > 1) {
                            b = sanitizeDate(l2.get(1));
                            resource.newResource("group")
                                .add("begindate", b);
                        }
                    } else if (m.groupCount() == 2) {
                        List<Integer> l = fractionDate(m.group(1) + m.group(2));
                        Integer b = sanitizeDate(l.get(0));
                        resource.newResource("group")
                                .add("begindate", b);
                        if (l.size() > 1) {
                            b = sanitizeDate(l.get(1));
                            resource.newResource("group")
                                .add("begindate", b);
                        }
                    } else {
                        resource.newResource("group")
                            .add("begindate", sanitizeDate(m.group(1)));
                    }
                    break;
                }
            }
            if (found) {
                continue;
            }
            if (movingwalls != null) {
                for (Pattern p : movingwalls) {
                    Matcher m = p.matcher(value);
                    found = m.find();
                    if (found) {
                        Integer b = DateUtil.getYear() - Integer.parseInt(m.group(1));
                        resource.newResource("group")
                                .add("begindate", b)
                                .add("open", "true");
                        break;
                    }
                }
            }
        }
        return resource;
    }

    public static Integer sanitizeDate(String date) {
        int pos = date.indexOf("/");
        if (pos > 0) {
            if (pos > 4) {
                return null; // invalid date
            }
            String s = date.substring(0, pos).replaceAll("[^\\d]","");
            return sanitizeDate(Integer.parseInt(s));
        } else {
            // remove non-numeric characters
            String s = date.replaceAll("[^\\d]","");
            return s.length() == 4 ? sanitizeDate(Integer.parseInt(s)) : null;
        }
    }

    public static Integer sanitizeDate(Integer date) {
        return date > 1500 && date <= DateUtil.getYear() ? date : null;
    }

    public static List<Integer> fractionDate(String date) {
        List<Integer> dates = new ArrayList();
        int pos = date.indexOf("/");
        if (pos > 0) {
            String s = date.substring(0, pos).replaceAll("[^\\d]","");
            int base = Integer.parseInt(s);
            dates.add(base);
            int frac = 0;
            try {
                frac = Integer.parseInt(date.substring(pos + 1));
            } catch (NumberFormatException e) {
                // not important
            }
            if (frac >= 100 && frac <= DateUtil.getYear()) {
                dates.add(frac);
            } else if (frac > 0 && frac < 100) {
                // two digits frac, create full year
                frac += Integer.parseInt(s.substring(0, 2)) * 100;
                dates.add(frac);
            }
        } else {
            String s = date.replaceAll("[^\\d]","");
            int base = Integer.parseInt(s);
            dates.add(base);
        }
        return dates;
    }

    public static Set<Integer> dates(Resource resource) {
        Set<Integer> dates = new TreeSet();
        Map m = resource.resources();
        Iterator<Collection<Resource>> it = m.values().iterator();
        while (it.hasNext()) {
            Collection<Resource> groups = it.next();
            for (Resource group : groups) {
                Object begindate = group.literal("begindate");
                Object enddate = group.literal("enddate");
                Object open = group.literal("open");
                List<Integer> starts;
                int start = -1;
                if (begindate != null) {
                    starts = fractionDate(begindate.toString());
                    dates.addAll(starts);
                    start = starts.get(0);
                }
                int end = -1;
                List<Integer> ends;
                if (enddate != null) {
                    ends = fractionDate(enddate.toString());
                    dates.addAll(ends);
                    end = ends.get(0);
                }
                if (open != null) {
                    end = DateUtil.getYear();
                }
                // add years from interval
                if (start >= 0 && end >= 0) {
                    if (start > DateUtil.getYear() || end > DateUtil.getYear()) {
                        logger.warn("future dates: {},{} (from {},{})",
                                start, end, begindate, enddate);
                    } else if (end - start > 250) {
                        logger.warn("too many years: {}-{} (from {},{})",
                                start, end, begindate, enddate);
                    // RDA: 1500
                    // Acta eruditorum 1682
                    // Phil. Trans. 1655 (but not in print)
                    } else if (start < 1500 || end < 1500) {
                        logger.warn("too early: {},{} ({},{})",
                                start, end, begindate, enddate);
                    } else {
                        for (int i = start; i <= end; i++) {
                            dates.add(i);
                        }
                    }
                }
                if (dates.size() > 250) {
                    logger.warn("too many dates: {}", dates.size());
                    break;
                }
            }
        }
        return dates;
    }
}
