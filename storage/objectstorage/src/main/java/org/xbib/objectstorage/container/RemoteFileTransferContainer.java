package org.xbib.objectstorage.container;

import org.xbib.io.Connection;
import org.xbib.io.ConnectionService;
import org.xbib.io.Session;
import org.xbib.io.ftp.apache.FTPSession;
import org.xbib.objectstorage.Action;
import org.xbib.objectstorage.ItemInfo;
import org.xbib.objectstorage.Request;
import org.xbib.objectstorage.action.Actions;
import org.xbib.objectstorage.action.ResponseCodeHeadAction;
import org.xbib.objectstorage.action.StateHeadAction;
import org.xbib.objectstorage.action.UploadItemJournalAction;
import org.xbib.objectstorage.request.FileRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Date;
import java.util.ResourceBundle;

public class RemoteFileTransferContainer extends AbstractContainer {

    private final static int BUFFER_SIZE = 8192;

    public RemoteFileTransferContainer(URI baseURI, ResourceBundle bundle) {
        super(baseURI, bundle);
    }

    @Override
    public Request newRequest() throws IOException {
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
        return new UploadItemJournalAction(getBundle() != null ? getBundle().getString("itemjournal") : null,
                itemInfo, this);
    }

    @Override
    public RemoteFileTransferContainer upload(ItemInfo info)
            throws IOException {
        if (info.getInputStream() == null) {
            return this;
        }
        String pathName = getBaseURI().toASCIIString();
        String fileName = info.getKey().getName();
        URL target = new URL(new URL(pathName), fileName);
        URI uri = getBaseURI();
        Connection<FTPSession> c = ConnectionService
                .getInstance()
                .getFactory(uri)
                .getConnection(uri);
        FTPSession session = c.createSession();
        session.open(Session.Mode.WRITE);
        // check for exist
        if (!session.exists(fileName)) {
            info.setCreationDate(new Date());
        } else {
            info.setModificationDate(new Date());
        }
        // make target dir
        session.mkdir(target.getPath());
        // copy file
        String targetFile = target.getFile();
        final byte[] buf = new byte[BUFFER_SIZE];
        MessageDigest md = createMessageDigest();
        md.reset();
        try (DigestOutputStream out = new DigestOutputStream(md, session, targetFile)) {
            int n;
            while ((n = info.getInputStream().read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            info.setOctets(out.getLength());
        }
        session.close();
        info.setChecksum(new BigInteger(1, md.digest()).toString(16));
        return this;
    }

    class DigestOutputStream extends OutputStream {

        FTPSession session;
        MessageDigest md;
        PipedOutputStream out;
        Thread worker;
        Throwable t;
        long len;

        DigestOutputStream(final MessageDigest md,
                           final FTPSession session,
                           final String target) throws IOException {
            this.md = md;
            this.session = session;
            this.len = 0L;
            final PipedInputStream in = new PipedInputStream(BUFFER_SIZE);
            this.out = new PipedOutputStream(in);
            this.worker = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        session.upload(in, target);
                    } catch (IOException e) {
                        t = e;
                    }
                }
            });
            worker.start();
        }

        @Override
        public void write(int b) throws IOException {
            write(new byte[] { (byte) b }, 0, 1);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            md.update(b, off, len);
            out.write(b, off, len);
            len += len - off;
            if (t != null) {
                throw new IOException(t);
            }
        }

        @Override
        public void close() throws IOException {
            out.close();
            try {
                worker.join(15000);
            } catch (InterruptedException e) {
                throw new IOException("interrupted in close");
            } finally {
                if (worker.isAlive() && !worker.isInterrupted()) {
                    worker.interrupt();
                }
            }
        }

        public long getLength() {
            return len;
        }
    }

}
