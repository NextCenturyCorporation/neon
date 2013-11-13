package com.ncc.neon.hive

import com.ncc.neon.config.field.FieldConfigurationMapping
import com.ncc.neon.metadata.MetadataConnection
import org.springframework.beans.factory.config.CustomScopeConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.support.SimpleThreadScope

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
 */

@Configuration
@ComponentScan(basePackages = ['com.ncc.neon'])
@Profile('hive-integrationtest')
class HiveIntegrationTestContext {

    static final String HOST_STRING = System.getProperty("hive.host", "xdata2")

    @Bean
    MetadataConnection metadataConnection(){
        return new MetadataConnection(System.getProperty("mongo.hosts", "localhost"))
    }

    @Bean
    FieldConfigurationMapping configurationBundle(){
        return new FieldConfigurationMapping()
    }

    @Bean
    CustomScopeConfigurer scopeConfigurer() {
        return new CustomScopeConfigurer(scopes: ["session":new SimpleThreadScope()])
    }


}
