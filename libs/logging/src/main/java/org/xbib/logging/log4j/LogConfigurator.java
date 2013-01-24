
package org.xbib.logging.log4j;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 */
public class LogConfigurator {

    private static boolean loaded;

    public static void configure(Map<String,String> map) {
        if (loaded) {
            return;
        }
        loaded = true;
        Properties props = new Properties();
        for (Iterator<Entry<String, String>> it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, String> entry = it.next();
            String key = "log4j." + entry.getKey();
            String value = entry.getValue();
            if (key.endsWith(".value")) {
                props.setProperty(key.substring(0, key.length() - ".value".length()), value);
            } else if (key.endsWith(".type")) {
                props.setProperty(key.substring(0, key.length() - ".type".length()), value);
            } else {
                props.setProperty(key, value);
            }
        }
        PropertyConfigurator.configure(props);
    }
}
