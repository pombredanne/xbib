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

package org.xbib.settings.loader;

/**
 * A settings loader factory automatically trying to identify what type of
 * {@link SettingsLoader} to use.
 *
 *
 */
public final class SettingsLoaderFactory {

    private SettingsLoaderFactory() {

    }

    /**
     * Returns a {@link SettingsLoader} based on the resource name.
     */
    public static SettingsLoader loaderFromResource(String resourceName) {
        return new PropertiesSettingsLoader();
    }

    /**
     * Returns a {@link SettingsLoader} based on the actual settings source.
     */
    public static SettingsLoader loaderFromSource(String source) {
        return new PropertiesSettingsLoader();
    }
}
