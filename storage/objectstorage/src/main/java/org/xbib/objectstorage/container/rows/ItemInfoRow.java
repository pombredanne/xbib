package org.xbib.objectstorage.container.rows;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URL;
import java.util.Date;

@XmlRootElement
public class ItemInfoRow {

    private String name;
    private String mimeType;
    private long octets;
    private String checksum;
    private Date creationDate;
    private Date modificationDate;
    private URL url;
    private String message;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setOctets(long octets) {
        this.octets = octets;
    }

    public long getOctets() {
        return octets;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setCreationDate(Date date) {
        this.creationDate = date;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setModificationDate(Date date) {
        this.modificationDate = date;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setURL(URL url) {
        this.url = url;
    }

    public URL getURL() {
        return url;
    }


}
