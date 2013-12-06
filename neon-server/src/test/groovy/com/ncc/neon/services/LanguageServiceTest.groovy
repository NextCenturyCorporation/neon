package com.ncc.neon.services
import com.ncc.neon.language.QueryParser
import com.ncc.neon.query.Query
import com.ncc.neon.query.QueryExecutor
import com.ncc.neon.query.QueryResult
import com.ncc.neon.query.TableQueryResult
import org.junit.Before
import org.junit.Test
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
 * @author tbrooks
 */

class LanguageServiceTest {

    private LanguageService languageService

    @Before
    void before() {
        languageService = new LanguageService()
        QueryExecutor executor = [execute: { query, options -> new TableQueryResult([["key1": "val1"], ["key2": 2]]) }] as QueryExecutor
        languageService.queryExecutorFactory = [getExecutor: { executor }] as QueryExecutorFactory

        QueryParser queryParser = [parse: { new Query() }] as QueryParser
        languageService.queryParser = queryParser
    }

    @Test
    void "execute query"() {
        QueryResult result = languageService.executeQuery("queryText")
        assert result.data
    }

}
