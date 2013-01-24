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
package org.xbib.atom;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class UUIDGenerator {

    private static final UUIDGenerator instance = new UUIDGenerator();
    private static final MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new Error("unable to instantiate MD5 message digest: " + e.getMessage());
        }
    }

    private UUIDGenerator() {
    }

    public static UUIDGenerator getInstance() {
        return instance;
    }

    public UUID generateNameBasedUUID(String name) {
        synchronized (digest) {
            return generateNameBasedUUID(name, digest);
        }
    }

    public UUID generateNameBasedUUID(String name, MessageDigest digest) {
        digest.reset();
        digest.update(name.getBytes());
        return UUID.nameUUIDFromBytes(digest.digest());
    }
}
