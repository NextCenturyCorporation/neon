package com.ncc.neon;
/*
 * ************************************************************************
 * Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 * This software code is the exclusive property of Next Century Corporation and is
 * protected by United States and International laws relating to the protection
 * of intellectual property.  Distribution of this software code by or to an
 * unauthorized party, or removal of any of these notices, is strictly
 * prohibited and punishable by law.
 *
 * UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 * SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 * ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND.  ANY USE BY YOU OF
 * THIS SOFTWARE CODE IS AT YOUR OWN RISK.  ALL WARRANTIES OF ANY KIND, EITHER
 * EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 * DISCLAIMED.
 *
 * PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 * OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 * RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 *
 * 
 */

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
