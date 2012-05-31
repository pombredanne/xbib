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
package org.xbib.io.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

    private static final String ISO_FORMAT_SECONDS = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String RFC_FORMAT = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
    private static final SimpleDateFormat isoFormat = new SimpleDateFormat();
    private static final SimpleDateFormat rfcFormat = new SimpleDateFormat();
    private static final TimeZone tz = TimeZone.getTimeZone("GMT");
    private static final Calendar cal = Calendar.getInstance();

    static {
        isoFormat.applyPattern(ISO_FORMAT_SECONDS);
        isoFormat.setTimeZone(tz);
        isoFormat.setLenient(true);
        rfcFormat.applyPattern(RFC_FORMAT);
        rfcFormat.setTimeZone(tz);
        rfcFormat.setLenient(true);
    }
    
    public static String formatNow() {
        return formatDateISO(new Date());
    }
    
    public synchronized static String formatDateISO(Date date) {
        if (date == null) {
            return null;
        }
        isoFormat.applyPattern(ISO_FORMAT_SECONDS);
        return isoFormat.format(date);
    }

    public synchronized static Date parseDateISO(String value) {
        if (value == null) {
            return null;
        }
        isoFormat.applyPattern(ISO_FORMAT_SECONDS);
        try {
            return isoFormat.parse(value);
        } catch (ParseException pe) {
            // skip
        }
        isoFormat.applyPattern("yyyy-MM-dd");
        try {
            return isoFormat.parse(value);
        } catch (ParseException pe) {
            return null;
        }
    }

    public synchronized static String formatDateRFC(Date date) {
        if (date == null) {
            return null;
        }
        return rfcFormat.format(date);
    }
    
    
    public synchronized static Date parseDateRFC(String value) {
        if (value == null) {
            return null;
        }
        try {
            return rfcFormat.parse(value);
        } catch (ParseException pe) {
            return null;
        }
    }

    public synchronized static int getYear(Date date) {
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    public synchronized static Date midnight() {
        return DateUtil.midnight(new Date());
    }

    public synchronized static Date midnight(Date date) {
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public synchronized static Date yesterday() {
        return DateUtil.yesterday(new Date());
    }

    public synchronized static Date yesterday(Date date) {
        return days(date, -1);
    }

    public synchronized static Date tomorrow() {
        return DateUtil.tomorrow(new Date());
    }

    public synchronized static Date tomorrow(Date date) {
        return days(date, 1);
    }

    public synchronized static Date years(int years) {
        return DateUtil.years(new Date(), years);
    }

    public synchronized static Date years(Date date, int years) {
        cal.setTime(date);
        cal.add(Calendar.YEAR, years);
        return cal.getTime();
    }
    
    public synchronized static Date months(int months) {
        return DateUtil.months(new Date(), months);
    }

    public synchronized static Date months(Date date, int months) {
        cal.setTime(date);
        cal.add(Calendar.MONTH, months);
        return cal.getTime();
    }
    
    public static Date weeks(int weeks) {
        return DateUtil.weeks(new Date(), weeks);
    }

    public synchronized static Date weeks(Date date, int weeks) {
        cal.setTime(date);
        cal.add(Calendar.WEEK_OF_YEAR, weeks);
        return cal.getTime();
    }

    public static Date days(int days) {
        return DateUtil.days(new Date(), days);
    }

    public synchronized static Date days(Date date, int days) {
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return cal.getTime();
    }

    public static Date hours(int hours) {
        return DateUtil.hours(new Date(), hours);
    }

    public synchronized static Date hours(Date date, int hours) {
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        return cal.getTime();
    }
    
    public static Date minutes(int minutes) {
        return DateUtil.minutes(new Date(), minutes);
    }

    public synchronized static Date minutes(Date date, int minutes) {
        cal.setTime(date);
        cal.add(Calendar.MINUTE, minutes);
        return cal.getTime();
    }
    
    public static Date seconds(int seconds) {
        return DateUtil.seconds(new Date(), seconds);
    }

    public synchronized static Date seconds(Date date, int seconds) {
        cal.setTime(date);
        cal.add(Calendar.MINUTE, seconds);
        return cal.getTime();
    }

}
