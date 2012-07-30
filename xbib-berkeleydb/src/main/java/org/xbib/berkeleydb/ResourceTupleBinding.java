/*
 * Licensed to Jörg Prante and xbib under one or more contributor 
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 * 
 * Copyright (C) 2012 Jörg Prante and xbib
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, see http://www.gnu.org/licenses/
 *
 */
package org.xbib.berkeleydb;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xbib.rdf.Resource;
import org.xbib.rdf.Statement;
import org.xbib.rdf.io.StatementListener;
import org.xbib.rdf.io.turtle.TurtleReader;
import org.xbib.rdf.io.turtle.TurtleWriter;
import org.xbib.rdf.simple.SimpleResource;

/**
 * A tuple-binding for resources in a Berkeley DB
 *
 */
public class ResourceTupleBinding extends TupleBinding
        implements StatementListener {

    private static final Logger logger = Logger.getLogger(ResourceTupleBinding.class.getName());
    private final TurtleWriter writer;
    private final TurtleReader reader;
    private long writtenChars;
    private long readChars;
    private Resource resource;

    public ResourceTupleBinding(URI baseURI) {
        this.writer = new TurtleWriter();
        this.reader = new TurtleReader(baseURI);
        this.writtenChars = 0;
        this.readChars = 0;
    }

    @Override
    public void objectToEntry(Object object, TupleOutput to) {
        try {
            StringWriter sw = new StringWriter();
            writer.write((Resource) object, false, sw);
            String s = sw.toString();
            int len = s.length();
            writtenChars += len;
            to.writeString(s);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    @Override
    public Object entryToObject(TupleInput ti) {
        try {
            resource = new SimpleResource();
            reader.setListener(this);
            String s = ti.readString();
            readChars += s.length();
            reader.parse(new StringReader(s));
            return resource;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        }
    }

    public long getWrittenChars() {
        return writtenChars;
    }

    public long getReadChars() {
        return readChars;
    }


    @Override
    public void statement(Statement statement) {
        resource.add(statement);
    }

    @Override
    public void newIdentifier(URI identifier) {
        // ignore
    }
}
