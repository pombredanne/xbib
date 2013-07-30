/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.xbib.settings;

import org.xbib.io.StreamInput;
import org.xbib.io.StreamOutput;
import org.xbib.settings.loader.SettingsLoader;
import org.xbib.settings.loader.SettingsLoaderFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * An immutable implementation of {@link Settings}.
 */
public class ImmutableSettings implements Settings {

    private Map<String, String> settings;

    private ImmutableSettings(Map<String, String> settings) {
        this.settings = ImmutableMap.copyOf(settings);
    }

    @Override
    public ImmutableMap<String, String> getAsMap() {
        return this.settings;
    }

    @Override
    public Settings getComponentSettings(Class component) {
        if (component.getName().startsWith("org.elasticsearch")) {
            return getComponentSettings("org.elasticsearch", component);
        }
        // not starting with org.elasticsearch, just remove the first package part (probably org/net/com)
        return getComponentSettings(component.getName().substring(0, component.getName().indexOf('.')), component);
    }

    @Override
    public Settings getComponentSettings(String prefix, Class component) {
        String type = component.getName();
        if (!type.startsWith(prefix)) {
            throw new SettingsException("Component [" + type + "] does not start with prefix [" + prefix + "]");
        }
        String settingPrefix = type.substring(prefix.length() + 1); // 1 for the '.'
        settingPrefix = settingPrefix.substring(0, settingPrefix.length() - component.getSimpleName().length()); // remove the simple class name (keep the dot)
        return getByPrefix(settingPrefix);
    }

    @Override
    public Settings getByPrefix(String prefix) {
        Builder builder = new Builder();
        for (Map.Entry<String, String> entry : getAsMap().entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                if (entry.getKey().length() < prefix.length()) {
                    // ignore this one
                    continue;
                }
                builder.put(entry.getKey().substring(prefix.length()), entry.getValue());
            }
        }
        return builder.build();
    }

    @Override
    public String get(String setting) {
        String retVal = settings.get(setting);
        if (retVal != null) {
            return retVal;
        }
        return null;
    }

    @Override
    public String get(String setting, String defaultValue) {
        String retVal = settings.get(setting);
        return retVal == null ? defaultValue : retVal;
    }

    @Override
    public Float getAsFloat(String setting, Float defaultValue) {
        String sValue = get(setting);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(sValue);
        } catch (NumberFormatException e) {
            throw new SettingsException("Failed to parse float setting [" + setting + "] with value [" + sValue + "]", e);
        }
    }

    @Override
    public Double getAsDouble(String setting, Double defaultValue) {
        String sValue = get(setting);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(sValue);
        } catch (NumberFormatException e) {
            throw new SettingsException("Failed to parse double setting [" + setting + "] with value [" + sValue + "]", e);
        }
    }

    @Override
    public Integer getAsInt(String setting, Integer defaultValue) {
        String sValue = get(setting);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(sValue);
        } catch (NumberFormatException e) {
            throw new SettingsException("Failed to parse int setting [" + setting + "] with value [" + sValue + "]", e);
        }
    }

    @Override
    public Long getAsLong(String setting, Long defaultValue) {
        String sValue = get(setting);
        if (sValue == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(sValue);
        } catch (NumberFormatException e) {
            throw new SettingsException("Failed to parse long setting [" + setting + "] with value [" + sValue + "]", e);
        }
    }

    @Override
    public Boolean getAsBoolean(String setting, Boolean defaultValue) {
        return Booleans.parseBoolean(get(setting), defaultValue);
    }


    @Override
    public String[] getAsArray(String settingPrefix) throws SettingsException {
        return getAsArray(settingPrefix, EMPTY_ARRAY);
    }

    @Override
    public String[] getAsArray(String settingPrefix, String[] defaultArray) throws SettingsException {
        List<String> result = Lists.newArrayList();

        if (get(settingPrefix) != null) {
            String[] strings = splitStringByCommaToArray(get(settingPrefix));
            if (strings.length > 0) {
                for (String string : strings) {
                    result.add(string.trim());
                }
            }
        }

        int counter = 0;
        while (true) {
            String value = get(settingPrefix + '.' + (counter++));
            if (value == null) {
                break;
            }
            result.add(value.trim());
        }
        if (result.isEmpty()) {
            return defaultArray;
        }
        return result.toArray(new String[result.size()]);
    }

    @Override
    public Map<String, Settings> getGroups(String settingPrefix) throws SettingsException {
        if (settingPrefix.charAt(settingPrefix.length() - 1) != '.') {
            settingPrefix = settingPrefix + ".";
        }
        // we don't really care that it might happen twice
        Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        for (Object o : settings.keySet()) {
            String setting = (String) o;
            if (setting.startsWith(settingPrefix)) {
                String nameValue = setting.substring(settingPrefix.length());
                int dotIndex = nameValue.indexOf('.');
                if (dotIndex == -1) {
                    throw new SettingsException("Failed to get setting group for [" + settingPrefix + "] setting prefix and setting [" + setting + "] because of a missing '.'");
                }
                String name = nameValue.substring(0, dotIndex);
                String value = nameValue.substring(dotIndex + 1);
                Map<String, String> groupSettings = map.get(name);
                if (groupSettings == null) {
                    groupSettings = new LinkedHashMap<String, String>();
                    map.put(name, groupSettings);
                }
                groupSettings.put(value, get(setting));
            }
        }
        Map<String, Settings> retVal = new LinkedHashMap<String, Settings>();
        for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
            retVal.put(entry.getKey(), new ImmutableSettings(Collections.unmodifiableMap(entry.getValue())));
        }
        return Collections.unmodifiableMap(retVal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImmutableSettings that = (ImmutableSettings) o;

        if (settings != null ? !settings.equals(that.settings) : that.settings != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = settings != null ? settings.hashCode() : 0;
        return result;
    }

    public static Settings readSettingsFromStream(StreamInput in) throws IOException {
        Builder builder = new Builder();
        int numberOfSettings = in.readVInt();
        for (int i = 0; i < numberOfSettings; i++) {
            builder.put(in.readString(), in.readString());
        }
        return builder.build();
    }

    public static void writeSettingsToStream(Settings settings, StreamOutput out) throws IOException {
        out.writeVInt(settings.getAsMap().size());
        for (Map.Entry<String, String> entry : settings.getAsMap().entrySet()) {
            out.writeString(entry.getKey());
            out.writeString(entry.getValue());
        }
    }

    /**
     * Returns a builder to be used in order to build settings.
     */
    public static Builder settingsBuilder() {
        return new Builder();
    }

    /**
     * A builder allowing to put different settings and then {@link #build()} an immutable
     * settings implementation. Use {@link ImmutableSettings#settingsBuilder()} in order to
     * construct it.
     */
    public static class Builder implements Settings.Builder {

        public static final Settings EMPTY_SETTINGS = new Builder().build();

        private final Map<String, String> map = new LinkedHashMap<String, String>();

        private Builder() {

        }

        public Map<String, String> internalMap() {
            return this.map;
        }

        /**
         * Removes the provided setting from the internal map holding the current list of settings.
         */
        public String remove(String key) {
            return map.remove(key);
        }

        /**
         * Returns a setting value based on the setting key.
         */
        public String get(String key) {
            String retVal = map.get(key);
            if (retVal != null) {
                return retVal;
            }
            // try camel case version
            return null; //map.get(toCamelCase(key));
        }

        /**
         * Sets a setting with the provided setting key and value.
         *
         * @param key   The setting key
         * @param value The setting value
         * @return The builder
         */
        public Builder put(String key, String value) {
            map.put(key, value);
            return this;
        }

        /**
         * Sets a setting with the provided setting key and class as value.
         *
         * @param key   The setting key
         * @param clazz The setting class value
         * @return The builder
         */
        public Builder put(String key, Class clazz) {
            map.put(key, clazz.getName());
            return this;
        }

        /**
         * Sets the setting with the provided setting key and the boolean value.
         *
         * @param setting The setting key
         * @param value   The boolean value
         * @return The builder
         */
        public Builder put(String setting, boolean value) {
            put(setting, String.valueOf(value));
            return this;
        }

        /**
         * Sets the setting with the provided setting key and the int value.
         *
         * @param setting The setting key
         * @param value   The int value
         * @return The builder
         */
        public Builder put(String setting, int value) {
            put(setting, String.valueOf(value));
            return this;
        }

        /**
         * Sets the setting with the provided setting key and the long value.
         *
         * @param setting The setting key
         * @param value   The long value
         * @return The builder
         */
        public Builder put(String setting, long value) {
            put(setting, String.valueOf(value));
            return this;
        }

        /**
         * Sets the setting with the provided setting key and the float value.
         *
         * @param setting The setting key
         * @param value   The float value
         * @return The builder
         */
        public Builder put(String setting, float value) {
            put(setting, String.valueOf(value));
            return this;
        }

        /**
         * Sets the setting with the provided setting key and the double value.
         *
         * @param setting The setting key
         * @param value   The double value
         * @return The builder
         */
        public Builder put(String setting, double value) {
            put(setting, String.valueOf(value));
            return this;
        }

        /**
         * Sets the setting with the provided setting key and an array of values.
         *
         * @param setting The setting key
         * @param values  The values
         * @return The builder
         */
        public Builder putArray(String setting, String... values) {
            remove(setting);
            int counter = 0;
            while (true) {
                String value = map.remove(setting + '.' + (counter++));
                if (value == null) {
                    break;
                }
            }
            for (int i = 0; i < values.length; i++) {
                put(setting + "." + i, values[i]);
            }
            return this;
        }

        /**
         * Sets the setting group.
         */
        public Builder put(String settingPrefix, String groupName, String[] settings, String[] values) throws SettingsException {
            if (settings.length != values.length) {
                throw new SettingsException("The settings length must match the value length");
            }
            for (int i = 0; i < settings.length; i++) {
                if (values[i] == null) {
                    continue;
                }
                put(settingPrefix + "." + groupName + "." + settings[i], values[i]);
            }
            return this;
        }

        /**
         * Sets all the provided settings.
         */
        public Builder put(Settings settings) {
            map.putAll(settings.getAsMap());
            return this;
        }

        /**
         * Sets all the provided settings.
         */
        public Builder put(Map<String, String> settings) {
            map.putAll(settings);
            return this;
        }

        /**
         * Sets all the provided settings.
         */
        public Builder put(Properties properties) {
            for (Map.Entry entry : properties.entrySet()) {
                map.put((String) entry.getKey(), (String) entry.getValue());
            }
            return this;
        }

        /**
         * Loads settings from the actual string content that represents them using the
         * {@link SettingsLoaderFactory#loaderFromSource(String)}.
         */
        public Builder loadFromSource(String source) {
            SettingsLoader settingsLoader = SettingsLoaderFactory.loaderFromSource(source);
            try {
                Map<String, String> loadedSettings = settingsLoader.load(source);
                put(loadedSettings);
            } catch (Exception e) {
                throw new SettingsException("Failed to load settings from [" + source + "]", e);
            }
            return this;
        }

        /**
         * Loads settings from a url that represents them using the
         * {@link SettingsLoaderFactory#loaderFromSource(String)}.
         */
        public Builder loadFromUrl(URL url) throws SettingsException {
            try {
                return loadFromStream(url.toExternalForm(), url.openStream());
            } catch (IOException e) {
                throw new SettingsException("Failed to open stream for url [" + url.toExternalForm() + "]", e);
            }
        }

        /**
         * Loads settings from a stream that represents them using the
         * {@link SettingsLoaderFactory#loaderFromSource(String)}.
         */
        public Builder loadFromStream(String resourceName, InputStream is) throws SettingsException {
            SettingsLoader settingsLoader = SettingsLoaderFactory.loaderFromResource(resourceName);
            try {
                Map<String, String> loadedSettings = settingsLoader.load(copyToString(new InputStreamReader(is, "UTF-8")));
                put(loadedSettings);
            } catch (Exception e) {
                throw new SettingsException("Failed to load settings from [" + resourceName + "]", e);
            }
            return this;
        }

        /**
         * Puts all the properties with keys starting with the provided <tt>prefix</tt>.
         *
         * @param prefix     The prefix to filter property key by
         * @param properties The properties to put
         * @return The builder
         */
        public Builder putProperties(String prefix, Properties properties) {
            for (Object key1 : properties.keySet()) {
                String key = (String) key1;
                String value = properties.getProperty(key);
                if (key.startsWith(prefix)) {
                    map.put(key.substring(prefix.length()), value);
                }
            }
            return this;
        }

        /**
         * Puts all the properties with keys starting with the provided <tt>prefix</tt>.
         *
         * @param prefix     The prefix to filter property key by
         * @param properties The properties to put
         * @return The builder
         */
        public Builder putProperties(String prefix, Properties properties, String[] ignorePrefixes) {
            for (Object key1 : properties.keySet()) {
                String key = (String) key1;
                String value = properties.getProperty(key);
                if (key.startsWith(prefix)) {
                    boolean ignore = false;
                    for (String ignorePrefix : ignorePrefixes) {
                        if (key.startsWith(ignorePrefix)) {
                            ignore = true;
                            break;
                        }
                    }
                    if (!ignore) {
                        map.put(key.substring(prefix.length()), value);
                    }
                }
            }
            return this;
        }

        /**
         * Runs across all the settings set on this builder and replaces <tt>${...}</tt> elements in the
         * each setting value according to the following logic:
         * <p/>
         * <p>First, tries to resolve it against a System property ({@link System#getProperty(String)}), next,
         * tries and resolve it against an environment variable ({@link System#getenv(String)}), and last, tries
         * and replace it with another setting already set on this builder.
         */
        public Builder replacePropertyPlaceholders() {
            PropertyPlaceholder propertyPlaceholder = new PropertyPlaceholder("${", "}", false);
            PropertyPlaceholder.PlaceholderResolver placeholderResolver = new PropertyPlaceholder.PlaceholderResolver() {
                @Override
                public String resolvePlaceholder(String placeholderName) {
                    String value = System.getProperty(placeholderName);
                    if (value != null) {
                        return value;
                    }
                    value = System.getenv(placeholderName);
                    if (value != null) {
                        return value;
                    }
                    return map.get(placeholderName);
                }
            };
            for (Map.Entry<String, String> entry : map.entrySet()) {
                map.put(entry.getKey(), propertyPlaceholder.replacePlaceholders(entry.getValue(), placeholderResolver));
            }
            return this;
        }

        /**
         * Builds a {@link Settings} (underlying uses {@link ImmutableSettings}) based on everything
         * set on this builder.
         */
        public Settings build() {
            return new ImmutableSettings(Collections.unmodifiableMap(map));
        }
    }
    
    public static final String[] EMPTY_ARRAY = new String[0];
    
    
    public static String[] splitStringByCommaToArray(final String s) {
        return splitStringToArray(s, ',');
    }
    
    public static String[] splitStringToArray(final String s, final char c) {
        if (s.length() == 0) {
            return EMPTY_ARRAY;
        }
        final char[] chars = s.toCharArray();
        int count = 1;
        for (final char x : chars) {
            if (x == c) {
                count++;
            }
        }
        final String[] result = new String[count];
        final int len = chars.length;
        int start = 0;  // starting index in chars of the current substring.
        int pos = 0;    // current index in chars.
        int i = 0;      // number of the current substring.
        for (; pos < len; pos++) {
            if (chars[pos] == c) {
                int size = pos - start;
                if (size > 0) {
                    result[i++] = new String(chars, start, size);
                }
                start = pos + 1;
            }
        }
        int size = pos - start;
        if (size > 0) {
            result[i++] = new String(chars, start, size);
        }
        if (i != count) {
            // we have empty strings, copy over to a new array
            String[] result1 = new String[i];
            System.arraycopy(result, 0, result1, 0, i);
            return result1;
        }
        return result;
    }    

    public static final int BUFFER_SIZE = 1024 * 8;
 
    public static String copyToString(Reader in) throws IOException {
        StringWriter out = new StringWriter();
        copy(in, out);
        return out.toString();
    }    
    
    public static int copy(Reader in, Writer out) throws IOException {
        try {
            int byteCount = 0;
            char[] buffer = new char[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
            return byteCount;
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                // do nothing
            }
            try {
                out.close();
            } catch (IOException ex) {
                // do nothing
            }
        }
    }
}
