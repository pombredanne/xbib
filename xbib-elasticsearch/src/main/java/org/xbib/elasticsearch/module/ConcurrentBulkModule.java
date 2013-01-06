package org.xbib.elasticsearch.module;

import org.elasticsearch.action.ActionModule;
import org.elasticsearch.action.bulk.TransportBulkAction;
import org.xbib.elasticsearch.action.bulk.concurrent.ConcurrentBulkAction;

public class ConcurrentBulkModule 
    extends ActionModule {

    public ConcurrentBulkModule() {
        super(true);
    }
    
    @Override
    protected void configure() {
        registerAction(ConcurrentBulkAction.INSTANCE, TransportBulkAction.class);
    }
}
