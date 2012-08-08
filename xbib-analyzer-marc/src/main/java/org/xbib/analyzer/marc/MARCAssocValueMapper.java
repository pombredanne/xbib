package org.xbib.analyzer.marc;

import org.xbib.elements.AssocValueMapper;

public class MARCAssocValueMapper extends AssocValueMapper {

    public MARCAssocValueMapper(String format) {
        super("/org/xbib/analyzer/elements/", format);
    }    
    
}
