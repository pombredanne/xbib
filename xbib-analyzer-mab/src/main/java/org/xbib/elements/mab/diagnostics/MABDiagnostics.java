package org.xbib.elements.mab.diagnostics;

import org.xbib.marc.FieldCollection;

public interface MABDiagnostics {
        
    void invalidLeader(FieldCollection key, String msg);

    void missingSatzTyp(FieldCollection key, String msg);
    
    void unknownRole(FieldCollection key, String value);

    void decodeCarrier(FieldCollection key, String msg, String value);
    
    void decodeType(FieldCollection key, String msg, String value);

    void unknownLibrary(FieldCollection key, String msg, String value);

    void invalidStandardNumber(FieldCollection key, String msg, String value);

    void relation(FieldCollection key, String type);
    
}
