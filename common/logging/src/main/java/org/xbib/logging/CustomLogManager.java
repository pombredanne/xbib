/**
 * Licensed to Jörg Prante and xbib under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with this work
 * for additional information regarding copyright ownership.
 *
 * Copyright (C) 2012 Jörg Prante and xbib
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public
 * License, these Appropriate Legal Notices must retain the display of the
 * "Powered by xbib" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by xbib".
 */
package org.xbib.logging;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Per classloader LogManager implementation. From the Apache Tomcat
 * documentation: The default implemenatation of java.util.logging provided in
 * the  JDK is too limited to be useful. A limitation of JDK Logging appears
 * to be the inability to have per-web application logging, as the
 * configuration is per-VM. As a result, Tomcat will,  in the default
 * configuration, replace the default LogManager  implementation with a
 * container friendly implementation called JULI,  which addresses these
 * shortcomings. It supports the same configuration  mechanisms as the
 * standard JDK java.util.logging, using either a  programmatic approach, or
 * properties files.  The main difference is that per-classloader properties
 * files can be set  (which enables easy redeployment friendly webapp
 * configuration), and the  properties files support slightly extended
 * constructs which allows more  freedom for defining handlers and assigning
 * them to loggers. JULI is enabled by default in Tomcat 5.5, and supports per
 * classloader configuration, in addition to the regular global
 * java.util.logging configuration. This means that logging can be  configured
 * at the following layers:
 *  <ul>
 *      <li>In the JDK's logging.properties file. Check your JAVA_HOME
 *      environment  setting to see which JDK Tomcat is using (or maybe JRE 5.0
 *      as Tomcat can  now run on a JRE from version 5.5). The file will be in
 *      $JAVA_HOME/jre/lib.  Alternately, it can also use a global
 *      configuration file located elsewhere  by using the system property
 *      java.util.logging.config.file,  or programmatic configuration using
 *      java.util.logging.config.class.</li>
 *      <li>In each classloader using a logging.properties file. This
 *      means  that it is possible to have a configuration for the Tomcat core,
 *      as well as separate configurations for each webapps which will have the
 *      same lifecycle as the webapps.</li>
 *  </ul>
 *  The default logging.properties specifies a ConsoleHandler for routing
 * logging to stdout and also a FileHandler. A handler's log level  threshold
 * can be set using SEVERE, WARNING, INFO, CONFIG, FINE,  FINER, FINEST or
 * ALL. The logging.properties shipped with JDK is set to INFO.  You can also
 * target specific packages to collect logging from and  specify a level. Here
 * is how you would set debugging from Tomcat.  You would need to ensure the
 * ConsoleHandler's level is also set to  collect this threshold, so FINEST or
 * ALL should be set.  Please refer to Sun's java.util.logging documentation
 * for the complete details. org.apache.catalina.level=FINEST The
 * configuration used by JULI is extremely similar, but uses a few  extensions
 * to allow better flexibility in assigning loggers.  The main differences
 * are:
 *  <ul>
 *      <li>A prefix may be added to handler names, so that multiple
 *      handlers  of a single class may be instantiated. A prefix is a String
 *      which starts  with a digit, and ends with '.'. For example, 22foobar.
 *      is a valid prefix.</li>
 *      <li>As in Java 5.0, loggers can define a list of handlers using
 *      the loggerName.handlers property.</li>
 *      <li>By default, loggers will not delegate to their parent if
 *      they have  associated handlers. This may be changed per logger using
 *      the  loggerName.useParentHandlers property, which accepts a boolean
 *      value.</li>
 *      <li>The root logger can define its set of handlers using  a
 *      .handlers property.</li>
 *      <li>System property replacement for property values which start
 *      with ${systemPropertyName}.</li>
 *  </ul>
 */
public class CustomLogManager extends LogManager {

    /**
     * Map containing the classloader information, keyed per
     * classloader. A weak hashmap is used to ensure no classloader reference
     * is leaked from  application redeployment.
     */
    protected final Map<ClassLoader, ClassLoaderLogInfo> classLoaderLoggers = new WeakHashMap<ClassLoader, ClassLoaderLogInfo>();
    /**
     * This prefix is used to allow using prefixes for the properties
     * names of handlers and their subcomponents.
     */
    protected ThreadLocal<String> prefix = new ThreadLocal<String>();

    /**
     * Add the specified logger to the classloader local configuration.
     *
     * @param logger The logger to be added
     *
     * @return true if the logger was added, false otherwise
     */
    @Override
    public synchronized boolean addLogger(final Logger logger) {
        final String loggerName = logger.getName();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ClassLoaderLogInfo info = getClassLoaderInfo(classLoader);

        if (info.loggers.containsKey(loggerName)) {
            return false;
        }

        info.loggers.put(loggerName, logger);

        // Apply initial level for new logger
        final String levelString = getProperty(loggerName + ".level");

        if (levelString != null) {
            try {
                AccessController.doPrivileged(new PrivilegedAction() {

                    @Override
                    public Object run() {
                        logger.setLevel(Level.parse(levelString.trim()));

                        return null;
                    }
                });
            } catch (IllegalArgumentException e) {
                // Leave level set to null
            }
        }

        // If any parent loggers have levels definied, make sure they are
        // instantiated
        int dotIndex = loggerName.lastIndexOf('.');

        while (dotIndex >= 0) {
            final String parentName = loggerName.substring(0, dotIndex);

            if (getProperty(parentName + ".level") != null) {
                Logger.getLogger(parentName);

                break;
            }

            dotIndex = loggerName.lastIndexOf('.', dotIndex - 1);
        }

        // Find associated node
        LogNode node = info.rootNode.findNode(loggerName);
        node.logger = logger;

        // Set parent logger
        Logger parentLogger = node.findParentLogger();

        if (parentLogger != null) {
            doSetParentLogger(logger, parentLogger);
        }

        // Tell children we are their new parent
        node.setParentLogger(logger);

        // Add associated handlers, if any are defined using the .handlers property.
        // In this case, handlers of the parent logger(s) will not be used
        String handlers = getProperty(loggerName + ".handlers");

        if (handlers != null) {
            logger.setUseParentHandlers(false);

            StringTokenizer tok = new StringTokenizer(handlers, ",");

            while (tok.hasMoreTokens()) {
                String handlerName = (tok.nextToken().trim());
                Handler handler = null;
                ClassLoader current = classLoader;

                while (current != null) {
                    info = classLoaderLoggers.get(current);

                    if (info != null) {
                        handler = info.handlers.get(handlerName);

                        if (handler != null) {
                            break;
                        }
                    }

                    current = current.getParent();
                }

                if (handler != null) {
                    logger.addHandler(handler);
                }
            }
        }

        // Parse useParentHandlers to set if the logger should delegate to its parent.
        // Unlike java.util.logging, the default is to not delegate if a list of handlers
        // has been specified for the logger.
        String useParentHandlersString = getProperty(loggerName + ".useParentHandlers");

        if (Boolean.valueOf(useParentHandlersString).booleanValue()) {
            logger.setUseParentHandlers(true);
        }

        return true;
    }

    /**
     * Get the logger associated with the specified name inside  the
     * classloader local configuration. If this returns null, and the call
     * originated for Logger.getLogger, a new logger with the specified name
     * will be instantiated and added using addLogger.
     *
     * @param name The name of the logger to retrieve
     *
     * @return the logger
     */
    public synchronized Logger getLogger(final String name) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        return getClassLoaderInfo(classLoader).loggers.get(name);
    }

    /**
     * Get an enumeration of the logger names currently defined in the
     * classloader local configuration.
     *
     * @return the logger names
     */
    @Override
    public synchronized Enumeration<String> getLoggerNames() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        return Collections.enumeration(getClassLoaderInfo(classLoader).loggers.keySet());
    }

    /**
     * Get the value of the specified property in the classloader local
     * configuration.
     *
     * @param name The property name
     *
     * @return the property value
     */
    @Override
    public String getProperty(String name) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String pref = this.prefix.get();

        if (pref != null) {
            name = pref + name;
        }

        ClassLoaderLogInfo info = getClassLoaderInfo(classLoader);
        String result = info.props.getProperty(name);

        // If the property was not found, and the current classloader had no 
        // configuration (property list is empty), look for the parent classloader
        // properties.
        if ((result == null) && (info.props.isEmpty())) {
            ClassLoader current = classLoader.getParent();

            while (current != null) {
                info = classLoaderLoggers.get(current);

                if (info != null) {
                    result = info.props.getProperty(name);

                    if ((result != null) || (!info.props.isEmpty())) {
                        break;
                    }
                }

                current = current.getParent();
            }

            if (result == null) {
                result = super.getProperty(name);
            }
        }

        // Simple property replacement (mostly for folder names)
        if (result != null) {
            result = replace(result);
        }

        return result;
    }

    /**
     * Read configuration
     *
     * @throws IOException if configuration can't be read
     * @throws SecurityException if configuration reading is not allowed
     */
    @Override
    public void readConfiguration() throws IOException, SecurityException {
        checkAccess();

        readConfiguration(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Read configuration from a specific input stream
     *
     * @param is the input stream
     *
     * @throws IOException if configuration can't be read
     * @throws SecurityException if configuration reading is not allowed
     */
    @Override
    public void readConfiguration(InputStream is) throws IOException, SecurityException {
        checkAccess();
        reset();

        readConfiguration(is, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Get string property
     *
     * @param name the name of the property
     * @param defaultValue the value if there is no such property
     *
     * @return the value of the property
     */
    protected String getStringProperty(String name, String defaultValue) {
        String val = getProperty(name);

        if (val == null) {
            return defaultValue;
        }

        return val.trim();
    }

    /**
     * Get integer property
     *
     * @param name the name of the property
     * @param defaultValue the value if there is no such property
     *
     * @return the value of the property
     */
    protected int getIntProperty(String name, int defaultValue) {
        String val = getProperty(name);

        if (val == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(val.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    /**
     * Get boolean property
     *
     * @param name the name of the property
     * @param defaultValue the value if there is no such property
     *
     * @return the value of the property
     */
    protected boolean getBooleanProperty(String name, boolean defaultValue) {
        String val = getProperty(name);

        if (val == null) {
            return defaultValue;
        }

        val = val.toLowerCase();

        if (val.equals("true") || val.equals("1")) {
            return true;
        } else if (val.equals("false") || val.equals("0")) {
            return false;
        }

        return defaultValue;
    }

    /**
     * Get level property
     *
     * @param name the name of the property
     * @param defaultValue the value if there is no such property
     *
     * @return the value of the property
     */
    protected Level getLevelProperty(String name, Level defaultValue) {
        String val = getProperty(name);

        if (val == null) {
            return defaultValue;
        }

        try {
            return Level.parse(val.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    /**
     * Get filter property
     *
     * @param name the name of the property
     * @param defaultValue the value if there is no such property
     *
     * @return the value of the property
     */
    protected Filter getFilterProperty(String name, Filter defaultValue) {
        String val = getProperty(name);

        try {
            if (val != null) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                Class clz = cl.loadClass(val);

                return (Filter) clz.newInstance();
            }
        } catch (Exception ex) {
        }

        return defaultValue;
    }

    /**
     * Get formatter property
     *
     * @param name the name of the property
     * @param defaultValue the value if there is no such property
     *
     * @return the value of the property
     */
    protected Formatter getFormatterProperty(String name, Formatter defaultValue) {
        String val = getProperty(name);

        try {
            if (val != null) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                Class clz = cl.loadClass(val);

                return (Formatter) clz.newInstance();
            }
        } catch (Exception ex) {
        }

        return defaultValue;
    }

    /**
     * Retrieve the configuration associated with the specified
     * classloader. If it does not exist, it will be created.
     *
     * @param classLoader The classloader for which we will retrieve or build
     *        the  configuration
     *
     * @return the class loader log info
     */
    protected ClassLoaderLogInfo getClassLoaderInfo(ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        ClassLoaderLogInfo info = classLoaderLoggers.get(classLoader);

        if (info == null) {
            final ClassLoader classLoaderParam = classLoader;
            AccessController.doPrivileged(new PrivilegedAction() {

                @Override
                public Object run() {
                    try {
                        readConfiguration(classLoaderParam);
                    } catch (IOException e) {
                        // Ignore
                        }

                    return null;
                }
            });
            info = classLoaderLoggers.get(classLoader);
        }

        return info;
    }

    /**
     * Read configuration for the specified classloader.
     *
     * @param classLoader
     *
     * @throws IOException Error
     */
    protected void readConfiguration(ClassLoader classLoader)
            throws IOException {
        InputStream is = null;

        // Special case for URL classloaders which are used in containers: 
        // only look in the local repositories to avoid redefining loggers 20 times
        if ((classLoader instanceof URLClassLoader) && (((URLClassLoader) classLoader).findResource("logging.properties") != null)) {
            is = classLoader.getResourceAsStream("logging.properties");
        }

        if ((is == null) && (classLoader == ClassLoader.getSystemClassLoader())) {
            String configFileStr = System.getProperty("java.util.logging.config.file");

            if (configFileStr != null) {
                try {
                    is = new FileInputStream(replace(configFileStr));
                } catch (IOException e) {
                    System.err.println("ERROR: " + e.getMessage());

                // Ignore
                }
            }

            // Try the default JVM configuration
            if (is == null) {
                File defaultFile = new File(new File(System.getProperty("java.home"), "lib"), "logging.properties");

                try {
                    is = new FileInputStream(defaultFile);
                } catch (IOException e) {
                    System.err.println("ERROR: " + e.getMessage());

                // Critical problem, do something ...
                }
            }
        }

        Logger localRootLogger = new RootLogger();

        if (is == null) {
            // Retrieve the root logger of the parent classloader instead
            ClassLoader current = classLoader.getParent();
            ClassLoaderLogInfo info = null;

            while ((current != null) && (info == null)) {
                info = getClassLoaderInfo(current);
                current = current.getParent();
            }

            if (info != null) {
                localRootLogger.setParent(info.rootNode.logger);
            }
        }

        ClassLoaderLogInfo info = new ClassLoaderLogInfo(new LogNode(null, localRootLogger));
        classLoaderLoggers.put(classLoader, info);

        if (is != null) {
            readConfiguration(is, classLoader);
        }

        addLogger(localRootLogger);
    }

    /**
     * Load specified configuration.
     *
     * @param is InputStream to the properties file
     * @param classLoader for which the configuration will be loaded
     *
     * @throws IOException If something wrong happens during loading
     */
    protected void readConfiguration(InputStream is, ClassLoader classLoader)
            throws IOException {
        ClassLoaderLogInfo info = classLoaderLoggers.get(classLoader);

        try {
            info.props.load(is);
        } catch (IOException e) {
            // Report error
            System.err.println("Configuration error");
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (Exception t) {
            }
        }

        // Create handlers for the root logger of this classloader
        String rootHandlers = info.props.getProperty(".handlers");
        String handlers = info.props.getProperty("handlers");
        Logger localRootLogger = info.rootNode.logger;

        if (handlers != null) {
            StringTokenizer tok = new StringTokenizer(handlers, ",");

            while (tok.hasMoreTokens()) {
                String handlerName = (tok.nextToken().trim());
                String handlerClassName = handlerName;
                String pref = "";

                if (handlerClassName.length() <= 0) {
                    continue;
                }

                // Parse and remove a prefix (prefix start with a digit, such as 
                // "10WebappFooHanlder.")
                if (Character.isDigit(handlerClassName.charAt(0))) {
                    int pos = handlerClassName.indexOf('.');

                    if (pos >= 0) {
                        pref = handlerClassName.substring(0, pos + 1);
                        handlerClassName = handlerClassName.substring(pos + 1);
                    }
                }

                try {
                    this.prefix.set(pref);

                    Handler handler = (Handler) classLoader.loadClass(handlerClassName).newInstance();
                    // The specification strongly implies all configuration should be done 
                    // during the creation of the handler object.
                    // This includes setting level, filter, formatter and encoding.
                    this.prefix.set(null);
                    info.handlers.put(handlerName, handler);

                    if (rootHandlers == null) {
                        localRootLogger.addHandler(handler);
                    }
                } catch (Exception e) {
                    // Report error
                    System.err.println("Handler error");
                    e.printStackTrace();
                }
            }

            // Add handlers to the root logger, if any are defined using the .handlers property.
            if (rootHandlers != null) {
                StringTokenizer tok2 = new StringTokenizer(rootHandlers, ",");

                while (tok2.hasMoreTokens()) {
                    String handlerName = (tok2.nextToken().trim());
                    Handler handler = info.handlers.get(handlerName);

                    if (handler != null) {
                        localRootLogger.addHandler(handler);
                    }
                }
            }
        }
    }

    /**
     * Set parent child relationship between the two specified loggers.
     *
     * @param logger the logger
     * @param parent the parent logger
     */
    protected static void doSetParentLogger(final Logger logger, final Logger parent) {
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                logger.setParent(parent);

                return null;
            }
        });
    }

    /**
     * System property replacement in the given string.
     *
     * @param str The original string
     *
     * @return the modified string
     */
    protected String replace(String str) {
        String result = str.trim();

        if (result.startsWith("${")) {
            int pos = result.indexOf('}');

            if (pos != -1) {
                String propName = result.substring(2, pos);
                String replacement = System.getProperty(propName);

                if (replacement != null) {
                    result = replacement + result.substring(pos + 1);
                }
            }
        }

        return result;
    }


    protected static final class LogNode {

        Logger logger;
        protected final Map<String, LogNode> children = new HashMap<String, LogNode>();
        protected final LogNode parent;

        LogNode(final LogNode parent, final Logger logger) {
            this.parent = parent;
            this.logger = logger;
        }

        LogNode(final LogNode parent) {
            this(parent, null);
        }

        LogNode findNode(String name) {
            LogNode currentNode = this;

            if (logger.getName().equals(name)) {
                return this;
            }

            while (name != null) {
                final int dotIndex = name.indexOf('.');
                final String nextName;

                if (dotIndex < 0) {
                    nextName = name;
                    name = null;
                } else {
                    nextName = name.substring(0, dotIndex);
                    name = name.substring(dotIndex + 1);
                }

                LogNode childNode = currentNode.children.get(nextName);

                if (childNode == null) {
                    childNode = new LogNode(currentNode);
                    currentNode.children.put(nextName, childNode);
                }

                currentNode = childNode;
            }

            return currentNode;
        }

        Logger findParentLogger() {
            Logger logger = null;
            LogNode node = parent;

            while ((node != null) && (logger == null)) {
                logger = node.logger;
                node = node.parent;
            }

            return logger;
        }

        void setParentLogger(final Logger parent) {
            for (final Iterator iter = children.values().iterator(); iter.hasNext();) {
                final LogNode childNode = (LogNode) iter.next();

                if (childNode.logger == null) {
                    childNode.setParentLogger(parent);
                } else {
                    doSetParentLogger(childNode.logger, parent);
                }
            }
        }
    }

    /**
     * Class loader log info
     */
    protected static final class ClassLoaderLogInfo {

        final LogNode rootNode;
        final Map<String, Logger> loggers = new HashMap<String, Logger>();
        final Map<String, Handler> handlers = new HashMap<String, Handler>();
        final Properties props = new Properties();

        ClassLoaderLogInfo(final LogNode rootNode) {
            this.rootNode = rootNode;
        }
    }

    /**
     * This class is needed to instantiate the root of each per
     * classloader  hierarchy.
     */
    protected class RootLogger extends Logger {

        /**
         * Creates a new RootLogger object.
         */
        public RootLogger() {
            super("", null);
        }
    }
}
