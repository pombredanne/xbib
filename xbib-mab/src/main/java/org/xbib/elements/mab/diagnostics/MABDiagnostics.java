package org.xbib.elements.mab.diagnostics;

import org.xbib.marc.FieldDesignatorList;

public interface MABDiagnostics {
        
    void invalidLeader(FieldDesignatorList key, String msg);

    void missingSatzTyp(FieldDesignatorList key, String msg);
    
    void unknownRole(FieldDesignatorList key, String value);

    void decodeCarrier(FieldDesignatorList key, String msg, String value);
    
    void decodeType(FieldDesignatorList key, String msg, String value);

    void unknownLibrary(FieldDesignatorList key, String msg, String value);

    void invalidStandardNumber(FieldDesignatorList key, String msg, String value);

    void relation(FieldDesignatorList key, String type);
    
}
