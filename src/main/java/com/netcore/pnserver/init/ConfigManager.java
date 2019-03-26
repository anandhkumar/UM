package com.netcore.pnserver.init;

/**
 * @author Hemant Adelkar
 *
 */

import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import org.apache.log4j.Logger;

/**
 * Fetches the config from a properties file on the file system. 
 */
public class ConfigManager {

    public static final String DEFAULT_PROPS_FILE = "/etc/pnserver.properties";
    private String propsFile;
    private Properties props;
    //default properties will be loaded from the classpath.
    private static String DEFAULT_PROPERTIES = "default.properties";
    private final Logger log = Logger.getLogger(ConfigManager.class.getName());
    private static final ConfigManager INSTANCE;

    static {
        String propsFile = System.getProperty("com.netcore.pnserver.propsfile");
        if (propsFile == null || propsFile.trim().length() == 0) {
            propsFile = DEFAULT_PROPS_FILE;
        }
        INSTANCE = new ConfigManager(propsFile);
    }

    private ConfigManager(String fileName) {
        this.propsFile = fileName;
        try {
            setup();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    public void reloadPropertie(){
        try {
            setup();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * lifecycle method that should be called only once before calling getproperty.
     * loads a default properties from the classpath and then the specified property file.
     * Thus any properties in the specified property file will override the default properties.
     */
    public void setup() throws IOException, NullPointerException, IllegalArgumentException {
   
        InputStream defIS = this.getClass().getResourceAsStream(DEFAULT_PROPERTIES);
        Properties defProps = new Properties();
        try {
            defProps.load(defIS);
        } catch (Exception e) {
            log.error("Error loading properties " + e.getMessage(), e);
            e.printStackTrace();
        }
        props = new Properties(defProps);
        props.load(new FileInputStream(propsFile));
    }

    public String getProperty(String propertyName) {
        return props.getProperty(propertyName);
    }

    public String getProperty(String propertyName, String defaultValue) {
        return props.getProperty(propertyName, defaultValue);
    }

    public void writeProperty(String key, String val) {
        props.setProperty(key, val);
        try {
            props.store(new FileOutputStream("/home/tomcat/etc/qmapserialized.lock"), null);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }
    }

    public Properties getProperties(String namespace) {
        Properties properts = new Properties();
        for (Enumeration keyEnum = props.propertyNames(); keyEnum.hasMoreElements();) {
            String key = (String) keyEnum.nextElement();
            if (key.startsWith(namespace)) {
                properts.setProperty(key, props.getProperty(key));
            }
        }
        return properts;
    }

    public HashSet<String> getkeys(String namespace) {
        HashSet<String> hset = new HashSet();
        for (Enumeration keyEnum = props.propertyNames(); keyEnum.hasMoreElements();) {
            String key = (String) keyEnum.nextElement();
            if (key.startsWith(namespace)) {
                hset.add(props.getProperty(key));
            }
        }
        return hset;
    }
}

