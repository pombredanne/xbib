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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import javax.activation.MimetypesFileTypeMap;

public class ItemInfo {

    private final static int BUFFER_SIZE = 8192;
    private Container container;
    private ItemKey key;
    private String mimeType;
    private long octets;
    private String checksum;
    private InputStream in;
    private Date creationDate;
    private Date modificationDate;
    private ItemMessage message;
    private boolean written;
    private URL url;

    private ItemInfo() {
    }
    
    public static ItemInfo newInfo(ObjectStorageAdapter adapter, Container container, String item) throws IOException {
        ItemInfo info = new ItemInfo();
        info.container = container;
        info.key = new ItemKey(item);
        info.url = URI.create(adapter.getBaseURI() + "/" + container.getName() + "/" + URLEncoder.encode(item, "UTF-8")).toURL();        
        return info;
    }

    public static ItemInfo getInfo(Path path) throws IOException {
        ItemInfo info = new ItemInfo();
        info.setKey(new ItemKey(path.getName(path.getNameCount() - 1).toString()));
        info.setModificationDate(new Date(Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis()));
        try {
            UserDefinedFileAttributeView view =
                    Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
            String key = "user.checksum";
            ByteBuffer buf = ByteBuffer.allocate(view.size(key));
            view.read(key, buf);
            buf.flip();
            String value = Charset.defaultCharset().decode(buf).toString();
            info.setChecksum(value);
        } catch (Exception e) {
        }
        return info;
    }

    public ItemInfo setKey(ItemKey key) {
        this.key = key;
        return this;
    }

    public ItemKey getKey() {
        return key;
    }

    public ItemInfo setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public String getMimeType() {
        return mimeType;
    }

    public ItemInfo setOctets(long octets) {
        this.octets = octets;
        return this;
    }

    public long getOctets() {
        return octets;
    }

    public ItemInfo setChecksum(String checksum) {
        this.checksum = checksum;
        return this;
    }

    public String getChecksum() {
        return checksum;
    }

    public ItemInfo setInputStream(InputStream in) {
        this.in = in;
        return this;
    }

    public InputStream getInputStream() {
        return in;
    }

    public ItemInfo setCreationDate(Date date) {
        this.creationDate = date;
        return this;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public ItemInfo setModificationDate(Date date) {
        this.modificationDate = date;
        return this;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public boolean isWrittenSuccessfully() {
        return written;
    }

    public void setItemMessage(ItemMessage message) {
        this.message = message;
    }

    public ItemMessage getMessage() {
        return message;
    }
    
    public URL getURL() {
        return url;    
    }
    
    public URL getDeleteURL() {
        return url;        
    }
    
    public synchronized boolean writeToFile(ObjectStorageAdapter adapter) throws IOException, NoSuchAlgorithmException {
        if (in == null) {
            return false;
        }
        written = false;
        File file = new File(container.createPath(adapter, key.getName()));
        boolean exists = file.exists();
        if (!exists) {
            file.getParentFile().mkdirs();
        }
        MessageDigest md = adapter.getMessageDigest();
        md.reset();
        long total;
        try (FileOutputStream out = new FileOutputStream(file)) {
            final byte[] buf = new byte[BUFFER_SIZE];
            total = 0L;
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
                md.update(buf, 0, n);
                total += n;
            }
            out.flush();
        }
        setOctets(total);
        setChecksum(new BigInteger(1, md.digest()).toString(16));
        if (!exists) {
            setCreationDate(new Date());
        } else {
            setModificationDate(new Date());
        }
        // set mime type if not explicitly given
        if (getMimeType() == null) {
            setMimeType(new MimetypesFileTypeMap().getContentType(file));
        }
        // write attributes
        try {
            Path path = FileSystems.getDefault().getPath(file.getPath(), file.getName());
            UserDefinedFileAttributeView view =
                    Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
            view.write("user.checksum", Charset.defaultCharset().encode(getChecksum()));
        } catch (Exception e) {           
        }
        written = true;
        return exists;
    }
}
