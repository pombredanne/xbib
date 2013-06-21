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
package org.xbib.tools.aggregator.elasticsearch.zdb.entities;

import org.xbib.date.DateUtil;
import org.xbib.grouping.bibliographic.work.PublishedJournal;
import org.xbib.map.MapBasedAnyObject;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Manifestation extends MapBasedAnyObject {

    private final String id;

    private final String targetID;

    private final String key;

    private final String title;

    private final String publisher;

    private final String language;

    private final String fromDate;

    private final String toDate;

    private final String description;

    private boolean head;

    private boolean part;

    private boolean hasPrintEdition;

    private String contentType;

    private String mediaType;

    private String carrierType;

    private boolean isSupplement;

    private String supplementID;

    private String supplementTargetID;

    private Map<String,Object> identifiers;

    private String unique;

    public Manifestation(Map<String, Object> m) {
        super(m);
        // we use DNB ID. ZDB ID collides with GND ID. Example: 21573803
        this.id = getString("IdentifierDNB.identifierDNB");
        this.targetID = getString("IdentifierZDB.identifierZDB");
        this.title = getString("TitleStatement.titleMain");
        this.publisher = getString("PublicationStatement.publisherName");
        this.language = getString("Language.value", "unknown");
        this.fromDate = getString("date1");
        this.toDate = getString("date2");
        this.head = (!map().containsKey("PrecedingEntry") && !map().containsKey("SucceedingEntry"))
            || (map().containsKey("PrecedingEntry") && !map().containsKey("SucceedingEntry"));
        this.part = map().containsKey("TitleStatement.titlePartName")
                || map().containsKey("TitleStatement.titlePartNumber");
        this.hasPrintEdition = findPrintEditionLink();
        this.description = getString("DatesOfPublication.value");
        // recognize supplement
        this.isSupplement = "isSupplementOf".equals(getString("SupplementParentEntry.relation"));
        if (isSupplement) {
            this.supplementID = getString("SupplementParentEntry.identifierDNB");
            this.supplementTargetID = getString("SupplementParentEntry.identifierZDB");
        } else {
            this.isSupplement = "isSupplementOf".equals(getString("SupplementSpecialEditionEntry.relation"));
            if (isSupplement) {
                this.supplementID = getString("SupplementSpecialEditionEntry.identifierDNB");
                this.supplementTargetID = getString("SupplementSpecialEditionEntry.identifierZDB");
            }
        }
        computeTypes();
        // last, compute key
        this.key = computeKey();
        this.identifiers = makeIdentifiers();
        // unique identifier
        this.unique = new PublishedJournal()
                .journalName(title)
                .publisherName(publisher)
                .createIdentifier();
    }

    public Manifestation(Manifestation m, String id, String targetID,
                  String contentType, String mediaType, String carrierType) {
        super(m.map());
        this.id = id;
        this.targetID = targetID;
        this.title = m.title();
        this.publisher = m.publisher();
        this.language = m.language();
        this.fromDate = m.fromDate();
        this.toDate = m.toDate();
        this.head = m.isHead();
        this.part = m.isPart();
        this.hasPrintEdition = m.hasPrintEdition();
        this.description = m.description();
        this.isSupplement = m.isSupplement();
        this.supplementID = m.supplementID();
        this.supplementTargetID = m.supplementTargetID();
        this.contentType = contentType;
        this.mediaType = mediaType;
        this.carrierType = carrierType;
        this.unique = m.getUniqueIdentifier();
        this.key = computeKey();
    }

    public String id() {
        return id;
    }

    public String targetID() {
        return targetID;
    }

    public Manifestation contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String contentType() {
        return contentType;
    }

    public Manifestation mediaType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public String mediaType() {
        return mediaType;
    }

    public Manifestation carrierType(String carrierType) {
        this.carrierType = carrierType;
        return this;
    }

    public String carrierType() {
        return carrierType;
    }

    public String title() {
        return title;
    }

    public String publisher() {
        return publisher;
    }

    public String language() {
        return language;
    }

    public String fromDate() {
        return fromDate;
    }

    public String toDate() {
        return toDate;
    }

    public String description() {
        return description;
    }

    public boolean isSupplement() {
        return isSupplement;
    }

    public String supplementID() {
        return supplementID;
    }

    public String supplementTargetID() {
        return supplementTargetID;
    }

    public boolean isHead() {
        return head;
    }

    public boolean isPart() {
        return part;
    }

    public boolean hasPrintEdition() {
        return hasPrintEdition;
    }

    public Map<String,Object> getIdentifiers() {
        return identifiers;
    }

    public String getUniqueIdentifier() {
        return unique;
    }

    private void computeTypes() {
        Object o = map().get("physicalDescriptionElectronicResource");
        if (o != null) {
            if (o instanceof List) {
                for (Object s : (List) o) {
                    this.contentType = "text";
                    this.mediaType = "computer";
                    this.carrierType = s.toString();
                    return;
                }
            } else {
                this.contentType = "text";
                this.mediaType = "computer";
                this.carrierType = "online resource".equals(o.toString()) ? "online resource" : "computer disc";
                return;
            }
        }
        // microform (plus unmediated text)
        if (map().containsKey("physicalDescriptionMicroform")) {
            this.contentType = "text";
            this.mediaType = "microform";
            this.carrierType = "other";
            return;
        }
        // before assuming unmediated text, check title strings for media phrases
        String[] phraseTitles = new String[]{
                getString("AdditionalPhysicalFormNote.value"),
                getString("otherCodes.genre"),
                getString("TitleStatement.titleMedium"),
                getString("TitleStatement.titlePartName"),
                getString("Note.value")
        };
        for (String s : phraseTitles) {
            if (s != null) {
                for (String t : ER) {
                    if (s.indexOf(t) >= 0) {
                        this.contentType = "text";
                        this.mediaType = "computer";
                        this.carrierType = "online resource";
                        return;
                    }
                }
                for (String t : MF) {
                    if (s.indexOf(t) >= 0) {
                        this.contentType = "text";
                        this.mediaType = "microform";
                        this.carrierType = "other";
                        return;
                    }
                }
            }
        }
        // default
        this.contentType = "text";
        this.mediaType = "unmediated";
        this.carrierType = "volume";
    }

    private final String[] ER = new String[]{
            "Elektronische Ressource"
    };

    private final String[] MF = new String[]{
            "Mikroform",
            "Microfiche",
            "secondary-microform"
    };

    private String computeKey() {
        StringBuilder sb = new StringBuilder();
        // precedence for text/unmediated/volume
        // contentType
        switch (contentType) {
            case "text": {
                sb.append("0");
                break;
            }
            default: { // non-text
                sb.append("1");
            }
        }
        // mediaType
        switch (mediaType) {
            case "unmediated": {
                sb.append("0");
                break;
            }
            default: { // microform, computer
                sb.append("1");
            }
        }
        // carrierType
        switch (carrierType) {
            case "volume": {
                sb.append("0");
                break;
            }
            default: { // online resource, computer disc, other
                sb.append("1");
                break;
            }
        }
        int delta;
        int d1;
        int d2 = 0;
        try {
            d1 = "9999".equals(fromDate) ? DateUtil.getYear() : Integer.parseInt(fromDate);
            d2 = "9999".equals(toDate) ? DateUtil.getYear() : Integer.parseInt(toDate);
            delta = d2 - d1;
        } catch (NumberFormatException e) {
            delta = 0;
        }
        return df.format(d2) + df.format(delta) + sb.toString();
    }

    private boolean findPrintEditionLink() {
        Object o = map().get("OtherEditionEntry");
        if (o == null) {
            return false;
        }
        if (!(o instanceof List)) {
            o = Arrays.asList(o);
        }
        for (Map<String,Object> m : (List<Map<String,Object>>)o) {
            if ("hasPrintEdition".equals(m.get("relation"))) {
                return true;
            }
        }
        return false;
    }

    private Map<String,Object> makeIdentifiers() {
        Map<String,Object> m = new HashMap();
        // get all ISSN
        m.put("issn", map().get("IdentifierISSN"));
        return m;
    }

    public String getKey() {
        return key;
    }

    public String toString() {
        return targetID;
    }

    private final DecimalFormat df = new DecimalFormat("0000");

    protected final ManifestationOrderComparator comparator = new ManifestationOrderComparator();

}

class ManifestationOrderComparator implements Comparator<Manifestation> {

    @Override
    public int compare(Manifestation m1, Manifestation m2) {
        if (m1 == m2) {
            return 0;
        }
        return (m2.getKey()).compareTo(m1.getKey());
    }
}