package org.xbib.analyzer.marc.diagnostics;

import org.xbib.marc.FieldList;

public interface MABDiagnostics {
        
    void invalidLeader(FieldList key, String msg);

    void missingSatzTyp(FieldList key, String msg);
    
    void unknownRole(FieldList key, String value);

    void decodeCarrier(FieldList key, String msg, String value);
    
    void decodeType(FieldList key, String msg, String value);

    void unknownLibrary(FieldList key, String msg, String value);

    void invalidStandardNumber(FieldList key, String msg, String value);

    void relation(FieldList key, String type);
    
}
