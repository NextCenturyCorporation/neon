package com.ncc.neon;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class NeonPropertiesLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(NeonPropertiesLoader.class);

    private final Properties properties;

    public NeonPropertiesLoader(){
        properties = new Properties();
        loadNeonProperties();
    }

    private void loadNeonProperties() {
        try{
            properties.load(NeonPropertiesLoader.class.getClassLoader().getResourceAsStream("neon.properties"));
        }
        catch(Exception ex){
            LOGGER.debug("Unable to load neon.properties, it is not on the classpath. Using default properties.");
        }
    }

    public String getNeonUrl(){
        return properties.getProperty("neon.url","https://localhost:9443/neon");
    }

    public String getOwfUrl(){
        return properties.getProperty("owf.url","https://localhost:8443/owf");
    }

}
