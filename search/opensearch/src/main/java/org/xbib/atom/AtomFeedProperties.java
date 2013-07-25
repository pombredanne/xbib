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
package org.xbib.atom;

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.provider.managed.FeedConfiguration;

public class AtomFeedProperties extends FeedConfiguration {

    private Abdera abdera;
    private String baseURI;
    private String contextPath;
    private String servicePath;
    private String title;
    private String subtitle;
    private String timepattern;
    private String stylesheet;
    private int from;
    private int size;

    public AtomFeedProperties(FeedConfiguration config) {
        super(config.getFeedId(), 
                config.getSubUri(), 
                config.getAdapterClassName(), 
                config.getFeedConfigLocation(),
                config.getServerConfiguration());
    }

    public void setAbdera(Abdera abdera) {
        this.abdera = abdera;
    }

    public Abdera getAbdera() {
        return abdera;
    }

    public void setBaseURI(String uri) {
        this.baseURI = uri;
    }

    public String getBaseURI() {
        return baseURI;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }

    public String getServicePath() {
        return servicePath;
    }
    public void setFrom(int from) {
        this.from = from;
    }

    public int getFrom() {
        return from;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
    
    public void setTitlePattern(String title) {
        this.title = title;
    }
    
    public String getTitlePattern() {
        return title;
    }
    public void setSubtitlePattern(String subtitle) {
        this.subtitle = subtitle;
    }
    
    public String getSubtitlePattern() {
        return subtitle;
    }
    
    public void setTimePattern(String pattern) {
        this.timepattern = pattern;
    }
    
    public String getTimePattern() {
        return timepattern;
    }
    
    
    public void setStylesheet(String sheet) {
        this.stylesheet = sheet;
    }
    
    public String getStyleSheet() {
        return stylesheet;
    }
}
