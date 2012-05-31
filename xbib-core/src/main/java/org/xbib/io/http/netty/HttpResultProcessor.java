package org.xbib.io.http.netty;

import org.xbib.io.ErrorResultProcessor;
import org.xbib.io.ResultProcessor;

public interface HttpResultProcessor 
    extends ResultProcessor<HttpResult>, ErrorResultProcessor<HttpResult>
{
}
