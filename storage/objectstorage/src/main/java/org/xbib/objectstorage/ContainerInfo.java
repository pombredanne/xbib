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
package org.xbib.objectstorage;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ContainerInfo {

    private final static Logger logger = Logger.getLogger(ContainerInfo.class.getName());

    private int objectCount;
    private long totalSize;
    private String name;
    private Pattern pattern;
    private File root;

    public ContainerInfo(ObjectStorageAdapter adapter, Container container) throws IOException {
        this(container.getName(), new File(container.createPath(adapter, "")));
    }

    /**
     * @param containerCount The number of objects in the container
     * @param totalSize      The total size of the container (in bytes)
     */
    public ContainerInfo(String name, File root) throws IOException {
        this.name = name;
        this.root = root;
        update();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public final synchronized void update() throws IOException {
        this.objectCount = 0;
        this.totalSize = 0L;
        try {
            traverse(root, new FileProcessor() {

                @Override
                public void process(File file) throws IOException {
                    setObjectCount(getObjectCount() + 1);
                    setTotalSize(getTotalSize() + file.length());

                }

            });
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void setObjectCount(int count) {
        this.objectCount = count;
    }

    /**
     * Returns the number of objects in the container
     *
     * @return The number of objects
     */
    public int getObjectCount() {
        return objectCount;
    }

    public void setTotalSize(long length) {
        this.totalSize = length;
    }

    /**
     * @return The total size of the objects in the container (in bytes)
     */
    public synchronized long getTotalSize() {
        this.totalSize = 0L;
        return totalSize;
    }

    public ContainerInfo setPattern(Pattern pattern) {
        this.pattern = pattern;
        return this;
    }

    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Returns the size as a human readable string, rounding to the nearest
     * KB/MB/GB
     *
     * @return The size of the object as a human readable string.
     */
    public String getSizeString() {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (totalSize > gb) {
            return (totalSize / gb) + " GB";
        } else if (totalSize > mb) {
            return (totalSize / mb) + " MB";
        } else if (totalSize > kb) {
            return (totalSize / kb) + " KB";
        } else {
            return totalSize + " Bytes";
        }
    }

    public void traverse(File file, FileProcessor processor) throws IOException {
        if (file.exists() && file.canRead()) {
            processor.process(file);
        }
        if (file.isDirectory()) {
            String[] list = file.list(new FilenameFilter() {

                @Override
                public boolean accept(File file, String string) {
                    return getPattern() != null ? getPattern().matcher(file.getName()).matches() : true;
                }

            });
            if (list != null) {
                for (String s : list) {
                    traverse(new File(file, s), processor);
                }
            }
        }
    }

    public interface FileProcessor {
        void process(File file) throws IOException;
    }


    public ItemInfo getItemInfo(Item item) throws IOException {
        // Files.getAttribute(null, name, null);
        return null;
    }


}
