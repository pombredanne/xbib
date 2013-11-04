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

import org.xbib.objectstorage.container.rows.ItemInfoRow;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

public class ItemInfo {

    private final Container container;

    private ItemKey key;

    private InputStream in;

    private ItemMessage message;

    private String mimeType;

    private long octets;

    private String checksum;

    private Date creationDate;

    private Date modificationDate;

    private boolean written;

    private URL url;

    public ItemInfo(Container container, String item) throws IOException {
        this.container = container;
        this.key = new ItemKey(item);
        this.url = URI.create(container.getBaseURI() + URLEncoder.encode(item, "UTF-8")).toURL();
    }

    /*public static ItemInfo getInfo(Path path) throws IOException {
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
    }*/

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


    public ItemInfoRow entity() {
        ItemInfoRow row = new ItemInfoRow();
        if (key != null) {
            row.setName(key.getName());
        }
        row.setChecksum(checksum);
        row.setCreationDate(creationDate);
        if (message != null) {
            row.setMessage(message.getMessage());
        }
        row.setMimeType(mimeType);
        row.setModificationDate(modificationDate);
        row.setOctets(octets);
        row.setURL(url);
        return row;
    }
}
