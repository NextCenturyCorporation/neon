package com.ncc.neon.hive

import com.ncc.neon.query.hive.HiveQueryBuilder
import com.ncc.neon.query.jdbc.JdbcClient
import com.ncc.neon.query.jdbc.JdbcQueryExecutor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/*
 *
 *  ************************************************************************
 *  Copyright (c), 2013 Next Century Corporation. All Rights Reserved.
 *
 *  This software code is the exclusive property of Next Century Corporation and is
 *  protected by United States and International laws relating to the protection
 *  of intellectual property. Distribution of this software code by or to an
 *  unauthorized party, or removal of any of these notices, is strictly
 *  prohibited and punishable by law.
 *
 *  UNLESS PROVIDED OTHERWISE IN A LICENSE AGREEMENT GOVERNING THE USE OF THIS
 *  SOFTWARE, TO WHICH YOU ARE AN AUTHORIZED PARTY, THIS SOFTWARE CODE HAS BEEN
 *  ACQUIRED BY YOU "AS IS" AND WITHOUT WARRANTY OF ANY KIND. ANY USE BY YOU OF
 *  THIS SOFTWARE CODE IS AT YOUR OWN RISK. ALL WARRANTIES OF ANY KIND, EITHER
 *  EXPRESSED OR IMPLIED, INCLUDING, WITHOUT LIMITATION, IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, ARE HEREBY EXPRESSLY
 *  DISCLAIMED.
 *
 *  PROPRIETARY AND CONFIDENTIAL TRADE SECRET MATERIAL NOT FOR DISCLOSURE OUTSIDE
 *  OF NEXT CENTURY CORPORATION EXCEPT BY PRIOR WRITTEN PERMISSION AND WHEN
 *  RECIPIENT IS UNDER OBLIGATION TO MAINTAIN SECRECY.
 * /
 */

/**
 * Created with IntelliJ IDEA.
 * User: mtamayo
 * Date: 4/28/13
 * Time: 11:26 PM
 * To change this template use File | Settings | File Templates.
 */
@Configuration
@ComponentScan(basePackages = ['com.ncc.neon'])
@Profile('hive-integrationtest')
class HiveIntegrationTestContext {

    @Bean
    JdbcQueryExecutor jdbcQueryExecutor() {
        return new JdbcQueryExecutor(new HiveQueryBuilder())
    }

    @Bean
    JdbcClient jdbcClient() {
        return new JdbcClient("org.apache.hadoop.hive.jdbc.HiveDriver", "hive", "default", "localhost:10000")
//        return new JdbcClient("org.apache.hadoop.hive.jdbc.HiveDriver", "hive2", "default", "localhost:10000")
    }

}
