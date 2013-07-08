package org.xbib.grouping.bibliographic.endeavor;

import org.testng.Assert;
import org.testng.annotations.Test;

public class WorkAuthorTest extends Assert {

    @Test
    public void test() throws Exception {
        assertFalse(new WorkAuthor().blacklist().isEmpty());

    }
}
