package org.xbib.elasticsearch.plugin;

import org.elasticsearch.action.ActionModule;
import org.elasticsearch.action.bulk.TransportBulkAction;
import org.elasticsearch.plugins.AbstractPlugin;
import org.xbib.elasticsearch.action.bulk.concurrent.ConcurrentBulkAction;

public class ConcurrentBulkPlugin extends AbstractPlugin {

    @Override
    public String name() {
        return "concurrent-bulk";
    }

    @Override
    public String description() {
        return "A concurrent variant of the bulk action";
    }
    
    public void onModule(ActionModule module) {
        module.registerAction(ConcurrentBulkAction.INSTANCE, TransportBulkAction.class);
    }
    
}
