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
package org.xbib.text;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Wraps ResourceBundle with a couple of additional, useful methods. Used for l10n
 */
public final class Localizer {

    private static Localizer instance = null;

    public static synchronized Localizer getInstance() {
        if (instance == null)
            instance = new Localizer();
        return instance;
    }

    public static synchronized void setInstance(Localizer localizer) {
        Localizer.instance = localizer;
    }

    public static String get(String key) {
        return getInstance().getValue(key);
    }

    public static String get(String key, String defaultValue) {
        return getInstance().getValue(key, defaultValue);
    }

    public static String sprintf(String key, Object... args) {
        return getInstance().sprintfValue(key, args);
    }

    private static final String DEFAULT_BUNDLE = "abderamessages";

    private final Locale locale;
    private final ResourceBundle bundle;

    public Localizer() {
        this(Locale.getDefault(), Thread.currentThread().getContextClassLoader());
    }

    public Localizer(Locale locale, ClassLoader loader) {
        this(initResourceBundle(DEFAULT_BUNDLE, locale, loader), locale);
    }

    public Localizer(String bundle) {
        this(initResourceBundle(bundle, Locale.getDefault(), Thread.currentThread().getContextClassLoader()));
    }

    public Localizer(String bundle, Locale locale) {
        this(initResourceBundle(bundle, locale, Thread.currentThread().getContextClassLoader()));
    }

    public Localizer(ResourceBundle bundle) {
        this(bundle, bundle.getLocale());
    }

    public Localizer(ResourceBundle bundle, Locale locale) {
        this.bundle = bundle;
        this.locale = locale;
    }

    private static ResourceBundle initResourceBundle(String bundle, Locale locale, ClassLoader loader) {
        try {
            return ResourceBundle.getBundle(bundle, locale, loader);
        } catch (Exception e) {
            return null;
        }
    }

    public Locale getLocale() {
        return locale;
    }

    public String getValue(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return null;
        }
    }

    public String getValue(String key, String defaultValue) {
        String value = getValue(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Use the JDK 1.5 sprintf style Formatter
     */
    public String sprintfValue(String key, Object... args) {
        String value = getValue(key);
        return value != null ? String.format(locale, value, args) : null;
    }
}
