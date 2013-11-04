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
package org.xbib.elasticsearch.tools.aggregate.zdb.entities;

import org.xbib.date.DateUtil;
import org.xbib.grouping.bibliographic.endeavor.PublishedJournal;
import org.xbib.map.MapBasedAnyObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Manifestation extends MapBasedAnyObject implements Comparable<Manifestation> {

    private final DecimalFormat df = new DecimalFormat("0000");

    protected final static Integer currentYear = GregorianCalendar.getInstance().get(GregorianCalendar.YEAR);

    private final String id;

    private final String externalID;

    private final String key;

    private String title;

    private final String publisher;

    private final String publisherPlace;

    private final String language;

    private List<String> country;

    private final Integer firstDate;

    private final Integer lastDate;

    private final String description;

    private final Map<String,Object> identifiers;

    private boolean isHead;

    private boolean isPart;

    private String printID;

    private String onlineID;

    private String printExternalID;

    private String onlineExternalID;

    private boolean hasPrint;

    private boolean hasOnline;

    private String contentType;

    private String mediaType;

    private String carrierType;

    private boolean isSupplement;

    private String supplementID;

    private String supplementExternalID;

    private String unique;

    private List links;

    public Manifestation(Map<String, Object> m) {
        super(m);
        // we use DNB ID. ZDB ID collides with GND ID. Example: 21573803
        this.id = getString("IdentifierDNB.identifierDNB");
        this.externalID = getString("IdentifierZDB.identifierZDB");
        buildTitle();
        this.publisher = getString("PublicationStatement.publisherName");
        this.publisherPlace = getString("PublicationStatement.placeOfPublication");
        this.language = getString("Language.value", "unknown");
        findCountry();
        Integer firstDate = getInteger("date1");
        this.firstDate = firstDate == null ? null : firstDate == 9999 ? null : firstDate;
        Integer lastDate = getInteger("date2");
        this.lastDate = lastDate == null ? null : lastDate == 9999 ? null : lastDate;
        this.isHead = !map().containsKey("SucceedingEntry");
        this.isPart = getString("TitleStatement.titlePartName") != null
                || getString("TitleStatement.titlePartNumber") != null;
        findPrintEditionLink();
        findOnlineEditionLink();
        findLinks();
        this.description = getString("DatesOfPublication.value");
        // recognize supplement
        this.isSupplement = "isSupplementOf".equals(getString("SupplementParentEntry.relation"));
        if (isSupplement) {
            this.supplementID = getString("SupplementParentEntry.identifierDNB");
            this.supplementExternalID = getString("SupplementParentEntry.identifierZDB");
        } else {
            this.isSupplement = "isSupplementOf".equals(getString("SupplementSpecialEditionEntry.relation"));
            if (isSupplement) {
                this.supplementID = getString("SupplementSpecialEditionEntry.identifierDNB");
                this.supplementExternalID = getString("SupplementSpecialEditionEntry.identifierZDB");
            }
        }
        // first, compute content types
        computeContentTypes();
        // last, compute key
        this.key = computeKey();
        this.identifiers = makeIdentifiers();
        // unique identifier
        StringBuilder p = new StringBuilder();
        if (publisher != null) {
            p.append(publisher);
        }
        if (publisherPlace != null && !publisherPlace.isEmpty()) {
            p.append('-').append(publisherPlace);
        }
        this.unique = new PublishedJournal()
                .journalName(title)
                .publisherName(p.toString())
                .createIdentifier();
    }

    public String id() {
        return id;
    }

    public String externalID() {
        return externalID;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }

    public String publisher() {
        return publisher;
    }

    public String publisherPlace() {
        return publisherPlace;
    }

    public String language() {
        return language;
    }

    public List<String> country() {
        return country;
    }

    public Integer firstDate() {
        return firstDate;
    }

    public Integer lastDate() {
        return lastDate;
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

    public String supplementExternalID() {
        return supplementExternalID;
    }

    public boolean isHead() {
        return isHead;
    }

    public boolean isPart() {
        return isPart;
    }

    public boolean hasPrint() {
        return hasPrint;
    }

    public boolean hasOnline() {
        return hasOnline;
    }

    public String getPrintID() {
        return printID;
    }

    public String getOnlineID() {
        return onlineID;
    }

    public String getPrintExternalID() {
        return printExternalID;
    }

    public String getOnlineExternalID() {
        return onlineExternalID;
    }

    public Map<String,Object> getIdentifiers() {
        return identifiers.isEmpty() ? null : identifiers;
    }

    public void setLinks(List<Map<String,Object>> links) {
        this.links = links;
    }

    public List<Map<String,Object>> getLinks() {
        return links;
    }

    public String getUniqueIdentifier() {
        return unique;
    }

    private void buildTitle() {
        String title = getString("TitleStatement.titleMain");
        // shorten title (series statement after '/' or ':') to raise probability of matching.
        int pos = title.indexOf('/');
        if (pos > 0) {
            title = title.substring(0, pos - 1);
        }
        pos = title.indexOf(':');
        if (pos > 0) {
            title = title.substring(0, pos - 1);
        }
        setTitle(title);
    }

    private void findLinks() {
        Object o = map().get("ElectronicLocationAndAccess");
        if (o != null) {
            if (!(o instanceof List)) {
                o = Arrays.asList(o);
            }
            this.links = (List)o;
            return;
        }
        this.links = Collections.EMPTY_LIST;
    }

    private void computeContentTypes() {
        Object o = map().get("physicalDescriptionElectronicResource");
        if (o != null) {
            if (o instanceof List) {
                List l = (List) o;
                for (Object s : l) {
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
                    if (s.contains(t)) {
                        this.contentType = "text";
                        this.mediaType = "computer";
                        this.carrierType = "online resource";
                        return;
                    }
                }
                for (String t : MF) {
                    if (s.contains(t)) {
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
            d1 = firstDate == null ? DateUtil.getYear() : firstDate;
            d2 = lastDate == null ? DateUtil.getYear() : lastDate;
            delta = d2 - d1;
        } catch (NumberFormatException e) {
            delta = 0;
        }
        return df.format(d2) + df.format(delta) + sb.toString();
    }

    private void findPrintEditionLink() {
        this.hasPrint = false;
        Object o = map().get("OtherEditionEntry");
        if (o == null) {
            return;
        }
        if (!(o instanceof List)) {
            o = Arrays.asList(o);
        }
        for (Map<String,Object> m : (List<Map<String,Object>>)o) {
            if ("hasPrintEdition".equals(m.get("relation"))) {
                this.hasPrint = true;
                this.printID = (String)m.get("identifierDNB");
                this.printExternalID = (String)m.get("identifierZDB");
                return;
            }
        }
    }

    private void findOnlineEditionLink() {
        this.hasOnline = false;
        Object o = map().get("OtherEditionEntry");
        if (o == null) {
            return;
        }
        if (!(o instanceof List)) {
            o = Arrays.asList(o);
        }
        for (Map<String,Object> m : (List<Map<String,Object>>)o) {
            if ("hasOnlineEdition".equals(m.get("relation"))) {
                this.hasOnline = true;
                this.onlineID = (String)m.get("identifierDNB");
                this.onlineExternalID = (String)m.get("identifierZDB");
                return;
            }
        }
    }

    private void findCountry() {
        Object o = getAnyObject("publishingCountry.isoCountryCodes");
        if (o instanceof List) {
            this.country = (List<String>)o;
        } else if (o instanceof String) {
            List<String> l = new ArrayList();
            l.add((String)o);
            this.country = l;
        } else {
            List<String> l = new ArrayList();
            l.add("unbekannt");
            this.country = l;
        }
    }

    private Map<String,Object> makeIdentifiers() {
        Map<String,Object> m = new HashMap();
        // get and convert all ISSN
        Object o = map().get("IdentifierISSN");
        if (o == null) {
            return m;
        }
        if (!(o instanceof List)) {
            o = Arrays.asList(o);
        }
        List<String> issns = new ArrayList();
        List<Map<String,Object>> l = (List<Map<String,Object>>)o;
        for (Map<String, Object> aL : l) {
            String s = (String) aL.get("value");
            if (s != null) {
                issns.add(s.replaceAll("\\-", "").toLowerCase());
            }
        }
        m.put("issn", issns);
        return m;
    }

    public String getKey() {
        return key;
    }

    public String toString() {
        return externalID;
    }

    @Override
    public int compareTo(Manifestation m) {
        return externalID.compareTo(m.externalID());
    }
    private final static IDComparator idComparator = new IDComparator();

    private static class IDComparator implements Comparator<Manifestation> {

        @Override
        public int compare(Manifestation m1, Manifestation m2) {
            if (m1 == m2) {
                return 0;
            }
            return m2.id().compareTo(m1.id());
        }
    }

    public static Comparator<Manifestation> getIdComparator() {
        return idComparator;
    }


}

