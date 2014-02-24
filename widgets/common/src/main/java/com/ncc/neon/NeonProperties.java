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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class NeonProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(NeonProperties.class);

    private static final NeonProperties INSTANCE;

    private final Properties properties;

    static {
        INSTANCE = new NeonProperties();
    }

    private NeonProperties() {
        properties = new Properties();
        loadNeonProperties();
    }

    private void loadNeonProperties() {
        try {
            properties.load(NeonProperties.class.getClassLoader().getResourceAsStream("neon.properties"));
            LOGGER.info("Found neon.properties on classpath");
        } catch (Exception ex) {
            LOGGER.info("Unable to load neon.properties, it is not on the classpath. Using default properties.");
        }
    }

    public static NeonProperties getInstance() {
        return INSTANCE;
    }

    public String getNeonUrl() {
        return properties.getProperty("neon.url", "https://localhost:9443/neon");
    }

    public String getOwfUrl() {
        return properties.getProperty("owf.url", "https://localhost:8443/owf");
    }

    public List<String> getHostnames() {
        String hostnames = properties.getProperty("hostnames", "");

        // splitting on an empty string will return a 1 element array with the empty string, which is not what
        // we want, so explicitly check for the hostnames
        if (!hostnames.isEmpty()) {
            return Arrays.asList(hostnames.split(","));
        }
        return Collections.emptyList();
    }

}
