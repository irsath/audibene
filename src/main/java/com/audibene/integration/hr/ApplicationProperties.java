package com.audibene.integration.hr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationProperties {
    private final static Logger logger = LoggerFactory.getLogger(ApplicationProperties.class);
    private final static Properties prop = new Properties();

    static {
        try (InputStream input = new FileInputStream("application.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            logger.error("Unable to load application properties.", ex);
        }
    }

    public static String get(String key) {
        String value = prop.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property " + key + "no found");
        } else {
            return value;
        }
    }
}
