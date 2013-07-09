package org.xbib.grouping.bibliographic.structure;

public class SerialComponentOrderKey {

    String volumeSpec;

    String issueSpec;

    String paginationSpec;

    public SerialComponentOrderKey() {

    }

    public SerialComponentOrderKey volume(String volumeSpec) {
        return this;
    }


}
