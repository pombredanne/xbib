package org.xbib.io.file;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public class PathFile {
    private Path path;
    private BasicFileAttributes attr;

    public PathFile(Path path, BasicFileAttributes attr) {
        this.path = path;
        this.attr = attr;
    }

    public Path getPath() {
        return path;
    }

    public BasicFileAttributes getAttributes() {
        return attr;
    }
}