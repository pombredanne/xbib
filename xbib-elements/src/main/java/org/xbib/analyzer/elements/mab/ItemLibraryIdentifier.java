package org.xbib.analyzer.elements.mab;

import java.util.List;
import java.util.Map;
import org.xbib.analyzer.marc.extensions.mab.MABBuilder;
import org.xbib.analyzer.marc.extensions.mab.MABContext;
import org.xbib.analyzer.marc.extensions.mab.MABElement;
import org.xbib.elements.ValueMapFactory;
import org.xbib.elements.items.*;
import org.xbib.marc.Field;
import org.xbib.marc.FieldCollection;

public class ItemLibraryIdentifier extends MABElement {

    private final static MABElement element = new ItemLibraryIdentifier();
    private final String defaultProvider = "DE-000";
    private final static Map<String, String> sigel2isil = 
            ValueMapFactory.getAssocStringMap("/org/xbib/analyzer/elements/","sigel2isil");
    private final static Map<String, Map<String, List<String>>> product2isil =
            ValueMapFactory.getMap("/org/xbib/analyzer/elements/","product2isil");

    private ItemLibraryIdentifier() {
    }

    public static MABElement getInstance() {
        return element;
    }

    @Override
    public ItemLibraryIdentifier build(MABBuilder b, FieldCollection key, String value) {
        boolean servicecreated = false;
        for (Field f : key) {
            switch (f.getSubfieldId()) {
                case "a":
                    resolveIdentifier(b, f.getData());
                    break;
                case "e":
                    createItemService(b, f.getData());
                    servicecreated = true;
                    break;
            }
        }
        if (!servicecreated) {
            createItemService(b, null);            
        }
        return this;
    }

    private String resolveIdentifier(MABBuilder b, String value) {
        if (product2isil.containsKey(value)) {
            for (String isil : product2isil.get(value).get("authorized")) {
                createISIL(b, isil, value);
            }
            createISIL(b, value, null);
        }
        String isil = sigel2isil.get(value);
        if (isil != null) {
            createISIL(b, isil, null);
        }
        return isil;
    }

    private void createISIL(MABBuilder b, String isil, String provider) {
        b.context().getResource(b.context().resource(), IDENTIFIER).add(XBIB_IDENTIFIER_AUTHORITY_ISIL, isil);
        if (provider == null) {
            provider = defaultProvider;
        }
        Authority authority = Authority.hbz;
        if (provider.startsWith("DE-")) {
            authority = Authority.ISIL;
        }
        b.context().access(new Access().name(provider, authority).library(new Library().library(isil, Authority.ISIL)));
    }

    private void createItemService(MABBuilder b, String itemStatus) {
        MABContext lia = b.context();
        String format = b.context().getFormat();
        boolean continuing = b.context().getContinuing();
        if (itemStatus == null) {
            // default service
            lia.getAccess().service(Service.INTER_LIBRARY_LOAN);
            lia.getAccess().service(Service.COPY);
            return;
        }
        if ("keine ILL".equals(itemStatus) || "keine Fernleihe".equals(itemStatus) || "Nicht ausleihbar".equals(itemStatus)) {
            lia.getAccess().service(Service.RESTRICTED);
        } else if ("Neuerwerbung".equals(itemStatus) || "In Bearbeitung".equals(itemStatus) || "Bestellt".equals(itemStatus) || "Neuerwerbungen".equals(itemStatus)) {
            lia.getAccess().service(Service.IN_PREPARATION);
        } else if ("Buchbinder".equals(itemStatus)) {
            lia.getAccess().service(Service.MAINTENANCE);
        } else if ("Ausgeschieden".equals(itemStatus) || "Ausgesondert".equals(itemStatus) || "Verlust".equals(itemStatus) || "Vermisst".equals(itemStatus) || "Löschen".equals(itemStatus)) {
            lia.getAccess().service(Service.WITHDRAWN);
        } else if ("Praesenzbestand".equals(itemStatus) || itemStatus.startsWith("Präsenz")) {
            lia.getAccess().service(Service.COPY);
        } else {
            lia.getAccess().service(Service.INTER_LIBRARY_LOAN);
            lia.getAccess().service(Service.COPY);
        }
        if (continuing) {
            // well, it's not a "book", but what then? 
            lia.getAccess().getItems().getLast().type(ItemType.UNKNOWN);
        }
        if (format == null) {
            return;
        }
        // adjust transport item
        if ("microform".equals(format)) {
            lia.getAccess().getItems().getLast().type(ItemType.MICROFORM);
        } else if ("game".equals(format)) {
            lia.getAccess().getItems().getLast().type(ItemType.GAME);
        } else if ("map".equals(format)) {
            lia.getAccess().getItems().getLast().type(ItemType.MAP);
        } else if ("audio".equals(format)) {
            lia.getAccess().getItems().getLast().type(ItemType.AUDIO);
        } else if ("electronic".equals(format)) {
            lia.getAccess().getItems().getLast().type(ItemType.ELECTRONIC_RESOURCE);
        } else if ("online".equals(format)) {
            // change transport/delivery preference if an online item is cataloged
            lia.getAccess().getItems().getLast().type(ItemType.ELECTRONIC_RESOURCE);
            lia.getAccess().getItems().getLast().transport(TransportMethod.ELECTRONIC);
            lia.getAccess().getItems().getLast().delivery(DeliveryMethod.ELECTRONIC);
        } else if (format.matches(".*video.*") && format.matches(".*audio.*")) {
            lia.getAccess().getItems().getLast().type(ItemType.AUDIO_VISUAL);
        } else if (format.matches(".*video.*")) {
            lia.getAccess().getItems().getLast().type(ItemType.VIDEO);
        }
    }
}
