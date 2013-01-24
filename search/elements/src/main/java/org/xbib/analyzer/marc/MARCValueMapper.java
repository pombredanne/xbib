package org.xbib.analyzer.marc;

import org.xbib.elements.AssocValueMapper;

public class MARCValueMapper extends AssocValueMapper {

    public MARCValueMapper(String format) {
        super("/org/xbib/analyzer/elements/", format);
    }    
    
}
