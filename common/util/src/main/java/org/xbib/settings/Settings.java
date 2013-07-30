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

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Immutable settings allowing to control the configuration.
 * <p/>
 * <p>Using {@link ImmutableSettings#settingsBuilder()} in order to create a builder
 * which in turn can create an immutable implementation of settings.
 *
 * @see ImmutableSettings
 */
public interface Settings {

    /**
     * Component settings for a specific component. Returns all the settings for the given class, where the
     * FQN of the class is used, without the <tt>org.elasticsearch<tt> prefix. If there is no <tt>org.elasticsearch</tt>
     * prefix, then the prefix used is the first part of the package name (<tt>org</tt> / <tt>com</tt> / ...)
     */
    Settings getComponentSettings(Class component);

    /**
     * Component settings for a specific component. Returns all the settings for the given class, where the
     * FQN of the class is used, without provided prefix.
     */
    Settings getComponentSettings(String prefix, Class component);

    /**
     * A settings that are filtered (and key is removed) with the specified prefix.
     */
    Settings getByPrefix(String prefix);


    /**
     * The settings as a {@link java.util.Map}.
     */
    ImmutableMap<String, String> getAsMap();

    /**
     * Returns the setting value associated with the setting key.
     *
     * @param setting The setting key
     * @return The setting value, <tt>null</tt> if it does not exists.
     */
    String get(String setting);

    /**
     * Returns the setting value associated with the setting key. If it does not exists,
     * returns the default value provided.
     *
     * @param setting      The setting key
     * @param defaultValue The value to return if no value is associated with the setting
     * @return The setting value, or the default value if no value exists
     */
    String get(String setting, String defaultValue);

    /**
     * Returns group settings for the given setting prefix.
     */
    Map<String, Settings> getGroups(String settingPrefix) throws SettingsException;

    /**
     * Returns the setting value (as float) associated with the setting key. If it does not exists,
     * returns the default value provided.
     *
     * @param setting      The setting key
     * @param defaultValue The value to return if no value is associated with the setting
     * @return The (float) value, or the default value if no value exists.
     * @throws SettingsException Failure to parse the setting
     */
    Float getAsFloat(String setting, Float defaultValue) throws SettingsException;

    /**
     * Returns the setting value (as double) associated with the setting key. If it does not exists,
     * returns the default value provided.
     *
     * @param setting      The setting key
     * @param defaultValue The value to return if no value is associated with the setting
     * @return The (double) value, or the default value if no value exists.
     * @throws SettingsException Failure to parse the setting
     */
    Double getAsDouble(String setting, Double defaultValue) throws SettingsException;

    /**
     * Returns the setting value (as int) associated with the setting key. If it does not exists,
     * returns the default value provided.
     *
     * @param setting      The setting key
     * @param defaultValue The value to return if no value is associated with the setting
     * @return The (int) value, or the default value if no value exists.
     * @throws SettingsException Failure to parse the setting
     */
    Integer getAsInt(String setting, Integer defaultValue) throws SettingsException;

    /**
     * Returns the setting value (as long) associated with the setting key. If it does not exists,
     * returns the default value provided.
     *
     * @param setting      The setting key
     * @param defaultValue The value to return if no value is associated with the setting
     * @return The (long) value, or the default value if no value exists.
     * @throws SettingsException Failure to parse the setting
     */
    Long getAsLong(String setting, Long defaultValue) throws SettingsException;

    /**
     * Returns the setting value (as boolean) associated with the setting key. If it does not exists,
     * returns the default value provided.
     *
     * @param setting      The setting key
     * @param defaultValue The value to return if no value is associated with the setting
     * @return The (boolean) value, or the default value if no value exists.
     * @throws SettingsException Failure to parse the setting
     */
    Boolean getAsBoolean(String setting, Boolean defaultValue) throws SettingsException;

    /**
     * The values associated with a setting prefix as an array. The settings array is in the format of:
     * <tt>settingPrefix.[index]</tt>.
     * <p/>
     * <p>It will also automatically load a comma separated list under the settingPrefix and merge with
     * the numbered format.
     *
     * @param settingPrefix The setting prefix to load the array by
     * @return The setting array values
     * @throws SettingsException
     */
    String[] getAsArray(String settingPrefix, String[] defaultArray) throws SettingsException;

    /**
     * The values associated with a setting prefix as an array. The settings array is in the format of:
     * <tt>settingPrefix.[index]</tt>.
     * <p/>
     * <p>It will also automatically load a comma separated list under the settingPrefix and merge with
     * the numbered format.
     *
     * @param settingPrefix The setting prefix to load the array by
     * @return The setting array values
     * @throws SettingsException
     */
    String[] getAsArray(String settingPrefix) throws SettingsException;

    /**
     * A settings builder interface.
     */
    interface Builder {

        /**
         * Builds the settings.
         */
        Settings build();
    }
}
