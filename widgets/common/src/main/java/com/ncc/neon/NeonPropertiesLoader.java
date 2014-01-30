/*
 * Copyright 2013 Next Century Corporation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
