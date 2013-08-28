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
package org.xbib.io.file;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * A finder for traversing PathFiles
 *
 */
public class Finder extends SimpleFileVisitor<Path> {

    private final PathMatcher matcher;

    private final LinkedList<PathFile> input = new LinkedList();

    private final EnumSet opts;

    private FileTime modifiedSince;

    private Comparator<PathFile> comparator;

    public Finder(String pattern) {
        this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        this.opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
    }

    public Finder(PathMatcher matcher, EnumSet opts) {
        this.matcher = matcher;
        this.opts = opts;
    }

    public Finder chronologicallySorted() {
        this.comparator = new Comparator<PathFile>(){
            public int compare(PathFile p1, PathFile p2) {
                return p1.getAttributes().lastModifiedTime().compareTo(p2.getAttributes().lastModifiedTime());
            } };
        return this;
    }

    public Finder pathSorted() {
        this.comparator = new Comparator<PathFile>(){
            public int compare(PathFile p1, PathFile p2) {
                return p1.getPath().toUri().compareTo(p2.getPath().toUri());
            } };
        return this;
    }

    public Queue<PathFile> getPathFiles() {
        if (comparator != null) {
            Collections.sort(input, comparator);
        }
        return input;
    }

    public Queue<URI> getURIs() {
        Queue<URI> uris = new ConcurrentLinkedQueue<URI>();
        for (PathFile p : getPathFiles()) {
            uris.add(p.getPath().toAbsolutePath().toUri());
        }
        return uris;
    }

    public Finder modifiedSince(long modifiedSince, TimeUnit tu) {
        this.modifiedSince = FileTime.from(modifiedSince, tu);
        return this;
    }

    public Finder find(String path) throws IOException {
        Files.walkFileTree(Paths.get(path), opts, Integer.MAX_VALUE, this);
        return this;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
        Path name = path.getFileName();
        if (name != null && matcher.matches(name)) {
            if (modifiedSince == null || attrs.lastModifiedTime().toMillis() > modifiedSince.toMillis()) {
                input.add(new PathFile(path,attrs));
            }
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }

}