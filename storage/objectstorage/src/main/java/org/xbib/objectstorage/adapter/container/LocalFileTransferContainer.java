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
package org.xbib.objectstorage.adapter.container;

import org.xbib.objectstorage.Action;
import org.xbib.objectstorage.ItemInfo;
import org.xbib.objectstorage.ObjectStorageRequest;
import org.xbib.objectstorage.action.Actions;
import org.xbib.objectstorage.action.ResponseCodeHeadAction;
import org.xbib.objectstorage.action.StateHeadAction;
import org.xbib.objectstorage.action.UploadItemJournalAction;
import org.xbib.objectstorage.adapter.container.request.FileRequest;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.security.MessageDigest;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class LocalFileTransferContainer extends AbstractContainer {

    private final static int BUFFER_SIZE = 8192;

    private Pattern pattern;

    public LocalFileTransferContainer(URI baseURI, ResourceBundle bundle) {
        super(baseURI, bundle);
    }


    @Override
    public ObjectStorageRequest newRequest() throws IOException {
        return new FileRequest();
    }


    @Override
    public boolean canUpload(String mimeType) {
        return "application/pdf".equals(mimeType);
    }

    @Override
    public Action getContainerHeadAction() {
        return new Actions(
                new ResponseCodeHeadAction(getBundle() != null ? getBundle().getString("itemstatus") : null),
                new StateHeadAction(getBundle() != null ? getBundle().getString("state") : null));
    }

    @Override
    public Action getItemJournalAction(ItemInfo itemInfo) {
        return new UploadItemJournalAction(getBundle() != null ? getBundle().getString("itemjournal") : null, itemInfo, this);
    }

    @Override
    public LocalFileTransferContainer upload(ItemInfo info)
                throws IOException {
        if (info.getInputStream() == null) {
            return this;
        }
        URL target = new URL(getBaseURI().toURL(), info.getKey().getName());
        File file = new File(target.toExternalForm());
        boolean exists = file.exists();
        if (!exists) {
            file.getParentFile().mkdirs();
        }
        MessageDigest md = createMessageDigest();
        md.reset();
        long total;
        // write File to file system
        try (FileOutputStream out = new FileOutputStream(file)) {
            final byte[] buf = new byte[BUFFER_SIZE];
            total = 0L;
            int n;
            while ((n = info.getInputStream().read(buf)) != -1) {
                out.write(buf, 0, n);
                md.update(buf, 0, n);
                total += n;
            }
            out.flush();
        }
        info.setOctets(total);
        info.setChecksum(new BigInteger(1, md.digest()).toString(16));
        if (!exists) {
            info.setCreationDate(new Date());
        } else {
            info.setModificationDate(new Date());
        }
        // set mime type if not explicitly given
        if (info.getMimeType() == null) {
            info.setMimeType(new MimetypesFileTypeMap().getContentType(file));
        }
        // write file attributes
        try {
            Path path = FileSystems.getDefault().getPath(file.getPath(), file.getName());
            UserDefinedFileAttributeView view =
                    Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
            view.write("user.checksum", Charset.defaultCharset().encode(info.getChecksum()));
        } catch (Exception e) {

        }
        return this;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Pattern getPattern() {
        return pattern;
    }

    private final synchronized void refresh() throws IOException {
        this.objectCount = 0;
        this.totalSize = 0L;
        File root = new File(getBaseURI());
        try {
            traverse(root, new FileProcessor() {

                @Override
                public void process(File file) throws IOException {
                    setObjectCount(getObjectCount() + 1);
                    setTotalSize(getTotalSize() + file.length());

                }

            });
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
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
}
