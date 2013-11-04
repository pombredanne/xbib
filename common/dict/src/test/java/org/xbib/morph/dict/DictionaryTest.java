package org.xbib.morph.dict;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xbib.dict.Dictionary;
import org.xbib.logging.Logger;
import org.xbib.logging.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class DictionaryTest extends Assert {

    private final static Logger logger = LoggerFactory.getLogger(DictionaryTest.class.getName());

    @Test
    public void testLemmatizer() throws Exception {
        InputStream in = DictionaryTest.class.getResourceAsStream("/de-lemma-utf8.txt");
        Reader reader = new InputStreamReader(in, "UTF-8");
        Dictionary db = new Dictionary().load(reader);
        assertEquals("der", db.lookup("der"));
        assertEquals("der", db.lookup("die"));
        assertEquals("der", db.lookup("das"));
        assertEquals("zurückgewinnen", db.lookup("zurückgewonnenen"));
        assertEquals("Auto", db.lookup("Autos"));
        assertEquals("Nudel", db.lookup("Nudeln"));
    }
}
