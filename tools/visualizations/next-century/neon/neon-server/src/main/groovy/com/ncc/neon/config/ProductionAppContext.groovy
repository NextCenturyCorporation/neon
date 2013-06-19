package com.ncc.neon.config

import com.mongodb.MongoClient
import com.ncc.neon.query.QueryExecutor
import com.ncc.neon.query.hive.HiveQueryBuilder
import com.ncc.neon.query.jdbc.JdbcClient
import com.ncc.neon.query.jdbc.JdbcQueryExecutor
import com.ncc.neon.query.mongo.MongoQueryExecutor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import org.springframework.core.env.PropertiesPropertySource

import javax.annotation.PostConstruct

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

/**
 * Spring bean configuration to use in production
 */
@Configuration
@PropertySource("classpath:neon.properties")
@Profile("production")
class ProductionAppContext {

    private static final def LOGGER = LoggerFactory.getLogger(ProductionAppContext)

    @Autowired
    private Environment environment

    @PostConstruct
    @SuppressWarnings('JavaIoPackageAccess') // suppress to allow local file overrides
    def postConstruct() {
        // TODO: NEON-89 Currently dev and production both use "production" as the environment (since we don't really have a production environment yet), which is why the override checks the user's home dir. In a real production environment, the file is likely to be somewhere else (such as /opt or /etc).  We can abstract this on a per environment basis.
        def override = new File(System.getProperty("user.home"), "neon/neon-override.properties")
        if (override.exists()) {
            def properties = new Properties()
            properties.load(new FileInputStream(override))
            environment.propertySources.addFirst(new PropertiesPropertySource("overrides", properties))
        }
    }

    @Bean
    QueryExecutor queryExecutor() {
        return new MongoQueryExecutor(mongo())
    }

    @Bean
    List<QueryExecutor> queryExecutors() {
        return [mongoQueryExecutor(), hiveQueryExecutor()]
    }

    @Bean
    MongoQueryExecutor mongoQueryExecutor() {
        return new MongoQueryExecutor(mongo())
    }

    @Bean
    JdbcQueryExecutor hiveQueryExecutor() {
        return new JdbcQueryExecutor(jdbcClient(), new HiveQueryBuilder())
    }

    @Bean
    Map<String, QueryExecutor> dataStores() {
        Map dataStores = [:]
        queryExecutors().each {
            dataStores.put(it.getDatastoreName(), it)
        }

        return dataStores
    }

    MongoClient mongo() {
        def hostsString = environment.getProperty("mongo.hosts")
        def serverAddresses = MongoConfigParser.createServerAddresses(hostsString)
        LOGGER.debug("connecting to mongo at {}",serverAddresses)
        return new MongoClient(serverAddresses)
    }

    JdbcClient jdbcClient() {
        String hostName = environment.getProperty("hive.host", "localhost")
        return new JdbcClient("org.apache.hive.jdbc.HiveDriver", "hive2", "default", hostName)
    }
}
